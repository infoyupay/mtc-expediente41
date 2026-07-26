package com.infoyupay.mtcexpediente41.javafx.treetable;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

import java.nio.file.Path;

/**
 * Displays the file-name component of a {@link Path} in a read-only JavaFX
 * tree-table cell.
 * <br/>
 * The cell retains the complete path as its value while presenting only the
 * final name component to the user.
 *
 * @param <S> row item type used by the tree table
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public class ReadOnlyTreeTablePathCell<S> extends TreeTableCell<S, Path> {

    /**
     * Creates a read-only path cell.
     */
    public ReadOnlyTreeTablePathCell() {
    }

    /**
     * Creates a cell factory for read-only path cells.
     *
     * @param <S> row item type used by the tree table
     * @return callback that creates a new path cell for each column request
     */
    public static <S> Callback<TreeTableColumn<S, Path>, TreeTableCell<S, Path>>
    forTreeTableColumn() {
        return _ -> new ReadOnlyTreeTablePathCell<>();
    }

    /**
     * Updates the text displayed for the current path value.
     *
     * @param item path assigned to the cell
     * @param empty whether the cell represents an empty row
     */
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
