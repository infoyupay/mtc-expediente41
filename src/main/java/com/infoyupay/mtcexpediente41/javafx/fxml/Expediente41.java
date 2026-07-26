package com.infoyupay.mtcexpediente41.javafx.fxml;

import com.infoyupay.mtcexpediente41.javafx.fxml.MainSceneController.MainScene;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Expediente41 extends Application {
    private MainScene controller;

    @Override
    public void start(Stage stage) throws IOException {
        controller = MainSceneController.fromFxml();
        stage.setScene(controller.root());
        stage.setTitle("Asistente de Archivos - Expediente DSTT-041 v1.0");
        stage.show();
    }
}
