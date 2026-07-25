package com.infoyupay.mtcexpediente41.datasheet;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Provides vehicle identifier readers according to workbook format.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class VehicleIdentifierReaders {

    /**
     * Prevents instantiation of this utility class.
     */
    private VehicleIdentifierReaders() {
    }

    /**
     * Creates the reader appropriate for the supplied workbook.
     *
     * @param workbook path to the workbook
     * @return reader suitable for the workbook format
     * @throws IllegalArgumentException if the workbook format is unsupported
     */
    @NotNull
    public static VehicleIdentifierReader forWorkbook(
            @NotNull Path workbook) {
        Objects.requireNonNull(workbook, "workbook");

        var fileName = workbook.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (fileName.endsWith(".xlsx")) {
            return new XlsxVehicleIdentifierReader();
        }

        if (fileName.endsWith(".ods")) {
            return new OdsVehicleIdentifierReader();
        }

        throw new IllegalArgumentException(
                "Unsupported spreadsheet format: " + fileName);
    }
}