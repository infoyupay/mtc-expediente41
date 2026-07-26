package com.infoyupay.mtcexpediente41;

import com.infoyupay.mtcexpediente41.javafx.fxml.Expediente41;
import javafx.application.Application;

/**
 * Provides the JVM entry point for the Expediente41 desktop application.
 * <br/>
 * The launcher delegates immediately to the JavaFX application lifecycle
 * without owning application initialization or user-interface state.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public class Launcher {

    /**
     * Creates a launcher instance.
     */
    public Launcher() {
    }

    /**
     * Launches the Expediente41 JavaFX application.
     *
     * @param args command-line arguments forwarded to JavaFX
     */
    static void main(String[] args) {
        Application.launch(Expediente41.class, args);
    }
}
