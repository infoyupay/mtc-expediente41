package com.infoyupay.mtcexpediente41.javafx.fxml;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javafx.scene.input.DragEvent.*;
import static javafx.scene.input.TransferMode.COPY_OR_MOVE;

/**
 * Handles drag-and-drop gestures that provide a directory.
 * <br/>
 * The dragged files are inspected when the gesture enters the target and the
 * first directory found is cached for the remaining events. This avoids
 * repeating filesystem checks for every {@link DragEvent#DRAG_OVER} event.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public abstract class OnlyDirectoryDropHandler implements EventHandler<DragEvent> {
    private boolean activeGesture;
    private Path lastDraggedDirectory;

    /**
     * Creates a directory-only drag-and-drop handler.
     */
    protected OnlyDirectoryDropHandler() {
    }

    /**
     * Dispatches supported drag event types to their gesture-specific handlers.
     *
     * @param event drag event to process
     */
    @Override
    public void handle(DragEvent event) {
        if (event.getEventType() == DRAG_ENTERED) {
            dragEntered(event);
        } else if (event.getEventType() == DRAG_OVER) {
            dragOver(event);
        } else if (event.getEventType() == DRAG_DROPPED) {
            dragDropped(event);
        } else if (event.getEventType() == DRAG_EXITED) {
            dragExited();
        }
    }

    /**
     * Inspects the dragboard once upon entering the target and caches the first
     * directory found.
     *
     * @param event drag-entered event carrying the dragboard
     */
    private void dragEntered(DragEvent event) {
        if (!activeGesture) {
            activeGesture = true;
            var dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                lastDraggedDirectory = dragboard
                        .getFiles()
                        .stream()
                        .map(File::toPath)
                        .filter(Files::isDirectory)
                        .findFirst()
                        .orElse(null);
            }
        }
    }

    /**
     * Marks the target as no longer participating in the current gesture.
     */
    private void dragExited() {
        activeGesture = false;
    }

    /**
     * Accepts the advertised transfer modes when the cached dragboard content
     * contains a directory.
     *
     * @param event drag-over event to accept or reject
     */
    private void dragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()
                && lastDraggedDirectory != null) {
            event.acceptTransferModes(COPY_OR_MOVE);
        }
        event.consume();
    }

    /**
     * Delivers the cached directory and reports whether the drop was completed.
     *
     * @param event drop event to complete
     */
    private void dragDropped(DragEvent event) {
        if (lastDraggedDirectory != null) {
            pathDropped(lastDraggedDirectory);
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        activeGesture = false;
        lastDraggedDirectory = null;
    }

    /**
     * Processes an accepted directory after it has been dropped.
     *
     * @param path dropped directory path
     */
    protected abstract void pathDropped(Path path);
}
