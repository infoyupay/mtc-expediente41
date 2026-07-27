package com.infoyupay.mtcexpediente41.javafx.fxml;

import com.infoyupay.mtcexpediente41.javafx.treetable.TreeTableVehicle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Controls the main workspace scene.
 * <br/>
 * The current controller presents the initial {@link TreeTableView} skeleton
 * and a proof-of-concept hierarchy that demonstrates the intended group,
 * vehicle, and document levels.
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
     * Populates the workspace tree with proof-of-concept data after a
     * workspace-selection request.
     */
    @FXML
    private void handleWorkspaceSelection() {
        chooseDir().ifPresent(this::readWorkspace);
    }

    private void readWorkspace(Path workspace){
        //TODO: make things happen.
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
        return Optional.ofNullable(chooser.showDialog(null))
                .map(File::toPath);
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

    private ExecutorService ioExecutor;

    /**
     * Returns the executor used to run blocking input/output tasks.
     *
     * @return configured input/output executor, or {@code null} before it is
     *         supplied by the application
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
}
