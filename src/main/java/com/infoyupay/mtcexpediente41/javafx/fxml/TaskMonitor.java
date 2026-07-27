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

/**
 * Displays the state, messages, and failures of a JavaFX {@link Task}.
 * <br/>
 * The monitor subscribes to one task, presents its messages in a diagnostic
 * console, and delegates failure-specific handling to subclasses.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public abstract class TaskMonitor extends Alert
        implements EventHandler<WorkerStateEvent> {

    private final TextArea console;
    private Task<?> task;

    /**
     * Creates an expanded informational dialog with a read-only diagnostic
     * console.
     */
    public TaskMonitor() {
        super(AlertType.INFORMATION);
        setContentText("Una tarea está en curso, para más detalles revisa la salida.");
        setTitle("Tarea en Curso");
        getButtonTypes().setAll(ButtonType.CLOSE);
        console = new TextArea();
        console.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        console.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        console.setPrefSize(400, 300);
        console.setEditable(false);
        console.setFont(Font.font("Monospaced", 12));
        getDialogPane().setExpandableContent(console);
        getDialogPane().setExpanded(true);
        getDialogPane().autosize();
    }

    /**
     * Attaches this monitor to a task and displays the monitoring dialog.
     *
     * @param task task whose state and messages will be observed
     */
    public void monitor(Task<?> task) {
        this.task = task;
        this.task.addEventHandler(WorkerStateEvent.ANY, this);
        this.task.messageProperty().subscribe(message -> {
            console.appendText(message + "\n");
            getDialogPane().setHeaderText(message);
        });
        show();
    }

    /**
     * Updates the dialog when the monitored task reaches a terminal state.
     *
     * @param event worker-state event emitted by the monitored task
     */
    @Override
    public void handle(WorkerStateEvent event) {
        if (event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
            getDialogPane().setHeaderText("Tarea completada exitosamente.");
        } else if (event.getEventType()
                == WorkerStateEvent.WORKER_STATE_FAILED) {
            processException(task.getException());
        } else if (event.getEventType()
                == WorkerStateEvent.WORKER_STATE_CANCELLED) {
            getDialogPane().setHeaderText("Tarea cancelada.");
        }
    }

    /**
     * Applies task-specific handling to a failure raised by the monitored task.
     *
     * @param throwable failure raised by the task
     */
    protected abstract void processException(Throwable throwable);

    /**
     * Appends a throwable stack trace to the diagnostic console.
     *
     * @param throwable throwable whose stack trace will be appended
     */
    protected void printStackTrace(Throwable throwable) {
        try (var sw = new StringWriter();
             var pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            console.appendText(sw.toString());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /**
     * Replaces the dialog header with a user-facing status message.
     *
     * @param message message to display
     */
    protected void setMessage(String message) {
        setHeaderText(message);
    }
}
