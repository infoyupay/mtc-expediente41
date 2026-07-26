package com.infoyupay.mtcexpediente41.javafx.treetable;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;

import java.nio.file.Path;
import java.util.function.Function;

public final class TreeTableVehicleValues {
    private static <S> ObjectBinding<S> alwaysNullBinding() {
        return Bindings.createObjectBinding(() -> null);
    }

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

    public static Callback<CellDataFeatures<TreeTableVehicle, String>, ObservableValue<String>> group(){
    return    createCellValueFactory(TreeTableVehicle::groupProperty);
    }

    public static Callback<CellDataFeatures<TreeTableVehicle, Path>, ObservableValue<Path>> brochure(){
        return createCellValueFactory(TreeTableVehicle::brochureProperty);
    }

    public static Callback<CellDataFeatures<TreeTableVehicle, String>, ObservableValue<String>> vin(){
        return createCellValueFactory(TreeTableVehicle::vinProperty);
    }

    public static Callback<CellDataFeatures<TreeTableVehicle, String>, ObservableValue<String>> document(){
        return createCellValueFactory(TreeTableVehicle::documentProperty);
    }

    public static Callback<CellDataFeatures<TreeTableVehicle, Path>, ObservableValue<Path>> pdf(){
        return createCellValueFactory(TreeTableVehicle::pdfProperty);
    }

}
