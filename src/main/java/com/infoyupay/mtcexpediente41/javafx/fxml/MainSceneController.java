package com.infoyupay.mtcexpediente41.javafx.fxml;

import com.infoyupay.mtcexpediente41.javafx.treetable.TreeTableVehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

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
     * Populates the workspace tree with proof-of-concept data after a
     * workspace-selection request.
     *
     * @param actionEvent event raised by the workspace selection control
     */
    @FXML
    private void handleWorkspaceSelection(ActionEvent actionEvent) {
        //Proof of concept. TODO: implement real stuff.
        var fake = new TreeItem<>(new TreeTableVehicle());
        //Group 1
        var group1 = new TreeItem<>(
                TreeTableVehicle.groupBranch("1", Path.of("1.1 Brochure Model A.pdf")));
        var vin1001 = new TreeItem<>(TreeTableVehicle.vinBranch("ABCDE1234WX001001"));
        vin1001.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("1.2 Special 1001.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("1.3 1001.pdf")))
        );
        var vin1002 = new TreeItem<>(TreeTableVehicle.vinBranch("ABCDE1234WX001002"));
        vin1002.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("1.2 Special 1002.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("1.3 1002.pdf")))
        );
        var vin1003 = new TreeItem<>(TreeTableVehicle.vinBranch("ABCDE1234WX001003"));
        vin1003.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("1.2 Special 1003.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("1.3 1003.pdf")))
        );
        var vin1004 = new TreeItem<>(TreeTableVehicle.vinBranch("ABCDE1234WX001004"));
        vin1004.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("1.2 Special 1004.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("1.3 1004.pdf")))
        );
        var vin1005 = new TreeItem<>(TreeTableVehicle.vinBranch("ABCDE1234WX001005"));
        vin1005.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("1.2 Special 1005.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("1.3 1005.pdf")))
        );
        group1.getChildren().addAll(vin1001, vin1002, vin1003, vin1004, vin1005);
        //Group2
        var group2 = new TreeItem<>(
                TreeTableVehicle.groupBranch("2", Path.of("2.1 Brochure Model B.pdf")));
        var vin1006 = new TreeItem<>(TreeTableVehicle.vinBranch("BBCDE1234WX001006"));
        vin1006.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("2.2 Special 1006.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("2.3 1006.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("4", Path.of("2.4 Fotos 1006.pdf")))
        );
        var vin1007 = new TreeItem<>(TreeTableVehicle.vinBranch("BBCDE1234WX001007"));
        vin1007.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("2.2 Special 1007.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("2.3 1007.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("4", Path.of("2.4 Fotos 1007.pdf")))
        );
        group2.getChildren().addAll(vin1006, vin1007);
        //Group3
        var group3 = new TreeItem<>(
                TreeTableVehicle.groupBranch("3", Path.of("3.1 Brochure Model C.pdf")));
        var vin1008 = new TreeItem<>(TreeTableVehicle.vinBranch("CBCDE1234WX001008"));
        group3.getChildren().add(vin1008);
        vin1008.getChildren().addAll(
                new TreeItem<>(TreeTableVehicle.documentLeaf("2", Path.of("3.2 Special 1008.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("3", Path.of("3.3 1008.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("4", Path.of("3.4 Fotos 1008.pdf"))),
                new TreeItem<>(TreeTableVehicle.documentLeaf("5", Path.of("3.5 Order 1008.pdf")))
        );
        //Finisher
        fake.getChildren().setAll(group1, group2, group3);
        tblWorkspace.setRoot(fake);
        expandAll(fake);
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
     * Represents a loaded main scene and its controller.
     *
     * @param root loaded JavaFX scene
     * @param controller controller associated with the scene
     *
     * @author David Vidal - InfoYupay SACS
     * @version 1.0
     */
    public record MainScene(Scene root, MainSceneController controller) {
    }
}
