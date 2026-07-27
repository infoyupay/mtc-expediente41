package com.infoyupay.mtcexpediente41.javafx.fxml;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static javafx.scene.input.DragEvent.*;
import static javafx.scene.input.TransferMode.COPY_OR_MOVE;

public abstract class OnlyDirectoryDropHandler implements EventHandler<DragEvent> {
    private boolean activeGesture;
    private Path lastDraggedDirectory;

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

    private void dragExited() {
        activeGesture = false;
    }

    private void dragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()
                && lastDraggedDirectory != null) {
            event.acceptTransferModes(COPY_OR_MOVE);
        }
        event.consume();
    }

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

    protected abstract void pathDropped(Path path);
}
