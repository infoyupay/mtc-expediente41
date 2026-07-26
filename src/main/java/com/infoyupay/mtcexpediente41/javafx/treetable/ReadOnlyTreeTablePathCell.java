package com.infoyupay.mtcexpediente41.javafx.treetable;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

import java.nio.file.Path;

public class ReadOnlyTreeTablePathCell<S> extends TreeTableCell<S, Path> {

    public static <S> Callback<TreeTableColumn<S, Path>, TreeTableCell<S, Path>>
    forTreeTableColumn() {
        return _ -> new ReadOnlyTreeTablePathCell<>();
    }

    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);
        //1. check empty cell/data
        if (empty || item == null) {
            setText("");
            setGraphic(null);
        } else {
            setText(item.getFileName().toString());
        }
    }

}
