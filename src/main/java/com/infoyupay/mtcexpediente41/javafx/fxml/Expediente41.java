package com.infoyupay.mtcexpediente41.javafx.fxml;

import com.infoyupay.mtcexpediente41.javafx.fxml.MainSceneController.MainScene;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Defines the JavaFX lifecycle for the Expediente41 desktop application.
 * <br/>
 * The application loads the main scene from FXML, retains its controller for
 * the lifetime of the primary stage, and presents the workspace assistant.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public class Expediente41 extends Application {
    private MainScene controller;

    /**
     * Creates the JavaFX application instance.
     * <br/>
     * User-interface initialization is deferred to {@link #start(Stage)}.
     */
    public Expediente41() {
    }

    /**
     * Loads and displays the primary application scene.
     *
     * @param stage primary stage supplied by the JavaFX runtime
     * @throws IOException if the main FXML resource cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        controller = MainSceneController.fromFxml();
        stage.setScene(controller.root());
        stage.setTitle("Asistente de Archivos - Expediente DSTT-041 v1.0");
        stage.show();
    }
}
