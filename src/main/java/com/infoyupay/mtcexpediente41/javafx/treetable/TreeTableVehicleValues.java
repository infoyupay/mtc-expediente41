package com.infoyupay.mtcexpediente41.javafx.treetable;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Provides type-safe cell-value factories for workspace tree-table columns.
 * <br/>
 * The factories expose the observable properties of
 * {@link TreeTableVehicle} rows to FXML without reflection-based property
 * names.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class TreeTableVehicleValues {

    /**
     * Creates a tree-table value factory provider.
     */
    public TreeTableVehicleValues() {
    }

    /**
     * Creates an observable binding whose value is always {@code null}.
     *
     * @param <S> binding value type
     * @return null-valued object binding
     */
    private static <S> ObjectBinding<S> alwaysNullBinding() {
        return Bindings.createObjectBinding(() -> null);
    }

    /**
     * Creates a null-safe tree-table cell-value factory.
     *
     * @param extractor function that selects an observable value from a row
     * @param <S> row item type
     * @param <T> column value type
     * @return cell-value factory backed by the supplied extractor
     */
    private static <S, T> Callback<CellDataFeatures<S, T>, ObservableValue<T>>
    createCellValueFactory(Function<S, ObservableValue<T>> extractor) {
        return feature -> {
            var item = feature.getValue();
            if (item == null) {
                return alwaysNullBinding();
            }
            var rowValue = item.getValue();
            if (rowValue == null) {
                return alwaysNullBinding();
            }
            return extractor.apply(rowValue);
        };
    }

    /**
     * Creates the value factory for the group column.
     *
     * @return group column value factory
     */
    public static Callback<CellDataFeatures<TreeTableVehicle, String>,
            ObservableValue<String>> group() {
        return createCellValueFactory(TreeTableVehicle::groupProperty);
    }

    /**
     * Creates the value factory for the brochure column.
     *
     * @return brochure column value factory
     */
    public static Callback<CellDataFeatures<TreeTableVehicle, Path>,
            ObservableValue<Path>> brochure() {
        return createCellValueFactory(TreeTableVehicle::brochureProperty);
    }

    /**
     * Creates the value factory for the vehicle identification number column.
     *
     * @return vehicle identification number column value factory
     */
    public static Callback<CellDataFeatures<TreeTableVehicle, String>,
            ObservableValue<String>> vin() {
        return createCellValueFactory(TreeTableVehicle::vinProperty);
    }

    /**
     * Creates the value factory for the document number column.
     *
     * @return document number column value factory
     */
    public static Callback<CellDataFeatures<TreeTableVehicle, String>,
            ObservableValue<String>> document() {
        return createCellValueFactory(TreeTableVehicle::documentProperty);
    }

    /**
     * Creates the value factory for the vehicle-specific PDF column.
     *
     * @return PDF path column value factory
     */
    public static Callback<CellDataFeatures<TreeTableVehicle, Path>,
            ObservableValue<Path>> pdf() {
        return createCellValueFactory(TreeTableVehicle::pdfProperty);
    }
}
