package com.infoyupay.mtcexpediente41.javafx.fxml;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;

public abstract class TaskMonitor extends Alert implements EventHandler<WorkerStateEvent> {

    private final TextArea console;
    private Task<?> task;

    public TaskMonitor() {
        super(AlertType.INFORMATION);
        setContentText("Una tarea está en curso, para más detalles revisa la salida.");
        setTitle("Tarea en Curso");
        getButtonTypes().setAll(ButtonType.CLOSE);
        console = new TextArea();
        console.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        console.setPrefSize(600, 400);
        console.setEditable(false);
        console.setFont(Font.font("Monospaced", 12));
        getDialogPane().setExpandableContent(console);
        getDialogPane().setExpanded(true);
    }

    public void monitor(Task<?> task) {
        this.task = task;
        this.task.addEventHandler(WorkerStateEvent.ANY, this);
        this.task.messageProperty().subscribe(message -> {
            console.appendText(message + "\n");
            getDialogPane().setHeaderText(message);
        });

    }

    @Override
    public void handle(WorkerStateEvent event) {
        if (event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
            getDialogPane().setHeaderText("Tarea completada exitosamente.");
        } else if (event.getEventType() == WorkerStateEvent.WORKER_STATE_FAILED) {
            processException(task.getException());
        } else if (event.getEventType() == WorkerStateEvent.WORKER_STATE_CANCELLED) {
            getDialogPane().setHeaderText("Tarea cancelada.");
        }
    }

    protected abstract void processException(Throwable t);

    protected void printStackTrace(Throwable t) {
        try (var sw = new StringWriter();
             var pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            console.appendText(sw.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void setMessage(String s) {
        setHeaderText(s);
    }
}
