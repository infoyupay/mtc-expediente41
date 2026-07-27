package com.infoyupay.mtcexpediente41.javafx.fxml;

import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysis;
import com.infoyupay.mtcexpediente41.datasheet.SheetVehicleIdentifier;
import com.infoyupay.mtcexpediente41.javafx.task.ReadWorkspaceTask;
import com.infoyupay.mtcexpediente41.javafx.task.UnsupportedWorkbookFormatException;
import com.infoyupay.mtcexpediente41.javafx.treetable.TreeTableVehicle;
import com.infoyupay.mtcexpediente41.pdf.GroupPdfDocument;
import com.infoyupay.mtcexpediente41.pdf.PdfDocumentNameException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Controls the main workspace scene.
 * <br/>
 * The controller reads a selected workspace outside the JavaFX application
 * thread and presents its group, vehicle, and document hierarchy in a
 * {@link TreeTableView}.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class MainSceneController {

    /**
     * Classpath location of the main scene FXML resource.
     */
    private static final String FXML =
            "/com/infoyupay/mtcexpediente41/javafx/fxml/main-scene.fxml";

    @FXML
    private Label lblSummary;

    @FXML
    private TextField txtFilter;

    @FXML
    private TreeTableView<TreeTableVehicle> tblWorkspace;

    private ExecutorService ioExecutor;
    private Stage primaryStage;
    private WorkspaceAnalysis analysis;

    /**
     * Creates a main scene controller.
     * <br/>
     * This constructor intentionally performs no UI initialization because
     * FXML fields are injected after controller construction.
     */
    public MainSceneController() {
    }

    /**
     * Loads the main scene and its controller from FXML.
     *
     * @return loaded scene and associated controller
     * @throws IOException if the FXML resource cannot be loaded
     */
    public static MainScene fromFxml() throws IOException {
        var resource = MainSceneController
                .class
                .getResource(FXML);
        Objects.requireNonNull(
                resource,
                "Resource not found: " + FXML);
        var loader = new FXMLLoader(resource);
        loader.load();
        return new MainScene(loader.getRoot(), loader.getController());
    }

    /**
     * Initializes drag-and-drop support after the FXML controls have been injected.
     */
    @FXML
    private void initialize() {
        tblWorkspace.addEventHandler(DragEvent.ANY, new OnlyDirectoryDropHandler() {
            /**
             * {@inheritDoc}
             */
            @Override
            protected void pathDropped(Path path) {
                if (path != null) {
                    readWorkspace(path);
                }
            }
        });
    }

    /**
     * Requests a workspace directory and starts reading it when selected.
     */
    @FXML
    private void handleWorkspaceSelection() {
        chooseDir().ifPresent(this::readWorkspace);
    }

    /**
     * Creates, monitors, and schedules a task that reads the selected workspace.
     *
     * @param workspace selected workspace root directory
     */
    private void readWorkspace(@NotNull Path workspace) {
        var executor = Objects.requireNonNull(
                ioExecutor,
                "Input/output executor has not been configured.");
        var task = new ReadWorkspaceTask(workspace);
        task.setOnSucceeded(_ -> showWorkspace(task.getValue()));
        var monitor = new TaskMonitor() {
            /**
             * Reports the task failure in the monitor and applies any
             * exception-specific handling.
             *
             * @param throwable failure raised while reading the workspace
             */
            @Override
            protected void processException(Throwable throwable) {
                switch (throwable) {
                    case Error _ -> Platform.exit();
                    case UnsupportedWorkbookFormatException formatException ->
                            setMessage("Se ha encontrado un tipo de archivo que no podemos leer en: "
                                    + formatException.getFileName());
                    case PdfDocumentNameException nameException ->
                            setMessage("El nombre de un archivo es inconsistente: " + nameException.getFileName());
                    default -> {
                    }
                }
                printStackTrace(throwable);
            }
        };
        if (primaryStage != null) {
            monitor.initOwner(primaryStage);
        }
        monitor.monitor(task);
        executor.execute(task);
    }

    /**
     * Presents a completed workspace analysis in the tree table.
     * <br/>
     * Each document group becomes a top-level branch, each analyzed vehicle
     * becomes a child branch, and its source PDFs become document leaves.
     *
     * @param analysis completed workspace analysis to present
     */
    private void showWorkspace(@NotNull WorkspaceAnalysis analysis) {
        this.analysis = Objects.requireNonNull(analysis, "analysis");

        var root = new TreeItem<>(new TreeTableVehicle());
        for (var group : analysis.groups()) {
            var brochure = group.brochure()
                    .map(GroupPdfDocument::path)
                    .orElse(null);
            var groupItem = new TreeItem<>(TreeTableVehicle.groupBranch(
                    Integer.toString(group.group()),
                    brochure));

            for (var vehicle : group.vehicles()) {
                var vin = vehicle.identifiers()
                        .stream()
                        .map(SheetVehicleIdentifier::vehicleIdentifier)
                        .collect(Collectors.joining(", "));
                if (vin.isEmpty()) {
                    vin = vehicle.marker();
                }

                var vehicleItem =
                        new TreeItem<>(TreeTableVehicle.vinBranch(vin));
                for (var document : vehicle.documents()) {
                    vehicleItem.getChildren().add(new TreeItem<>(
                            TreeTableVehicle.documentLeaf(
                                    Integer.toString(
                                            document.documentNumber()),
                                    document.path())));
                }
                groupItem.getChildren().add(vehicleItem);
            }
            root.getChildren().add(groupItem);
        }

        if (!analysis.ungroupedVehicleIdentifiers().isEmpty()) {
            var ungroupedItem = new TreeItem<>(
                    TreeTableVehicle.groupBranch("Sin grupo", null));
            for (var identifier
                    : analysis.ungroupedVehicleIdentifiers()) {
                ungroupedItem.getChildren().add(new TreeItem<>(
                        TreeTableVehicle.vinBranch(
                                identifier.vehicleIdentifier())));
            }
            root.getChildren().add(ungroupedItem);
        }

        tblWorkspace.setRoot(root);
        lblSummary.setText(
                "Lote: %d vehículos identificados / %d grupos detectados"
                        .formatted(
                                analysis.vehicleCount(),
                                analysis.groupCount()));
        expandAll(root);
    }

    /**
     * Recursively expands one tree item and all of its descendants.
     *
     * @param root root of the subtree to expand, or {@code null}
     */
    private void expandAll(TreeItem<TreeTableVehicle> root) {
        if (root == null) return;
        root.setExpanded(true);
        if (root.getChildren().isEmpty()) return;
        for (var item : root.getChildren()) {
            item.setExpanded(true);
            expandAll(item);
        }
    }

    /**
     * Requests confirmation and exits the application when the user accepts.
     */
    @FXML
    private void handleExit() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación Requerida");
        alert.setHeaderText("¿Está Seguro que desea salir de la app?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.showAndWait()
                .filter(ButtonType.YES::equals)
                .ifPresent(_ -> Platform.exit());
    }

    /**
     * Clears the loaded workspace hierarchy and the active VIN filter.
     */
    @FXML
    private void handleClean() {
        tblWorkspace.setRoot(null);
        txtFilter.clear();
    }

    /**
     * Clears the VIN filter text.
     */
    @FXML
    private void handleCleanFilter() {
        txtFilter.clear();
    }

    /**
     * Opens a directory chooser for selecting a workspace.
     *
     * @return the selected directory path, or an empty optional when cancelled
     */
    private Optional<Path> chooseDir() {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Abrir workspace...");
        return Optional.ofNullable(chooser.showDialog(primaryStage))
                .map(File::toPath);
    }

    /**
     * Returns the executor used to run blocking input/output tasks.
     *
     * @return configured input/output executor, or {@code null} before it is
     * supplied by the application
     */
    public ExecutorService getIoExecutor() {
        return ioExecutor;
    }

    /**
     * Sets the executor used to run blocking input/output tasks.
     *
     * @param ioExecutor executor supplied by the application
     */
    public void setIoExecutor(ExecutorService ioExecutor) {
        this.ioExecutor = ioExecutor;
    }

    /**
     * Returns the primary stage used to own dialogs opened by this controller.
     *
     * @return configured primary stage, or {@code null} before it is supplied
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Sets the primary stage used to own dialogs opened by this controller.
     *
     * @param primaryStage primary application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleGenerate() {

    }

    /**
     * Represents a loaded main scene and its controller.
     *
     * @param root       loaded JavaFX scene
     * @param controller controller associated with the scene
     * @author David Vidal - InfoYupay SACS
     * @version 1.0
     */
    public record MainScene(Scene root, MainSceneController controller) {
    }
}
