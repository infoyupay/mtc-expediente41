package com.infoyupay.mtcexpediente41.datasheet;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads vehicle identifiers from spreadsheet workbooks.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public sealed interface VehicleIdentifierReader
        permits XlsxVehicleIdentifierReader,
        OdsVehicleIdentifierReader {

    /**
     * Reads worksheet names and vehicle identifiers from a workbook.
     *
     * @param workbook path to the workbook
     * @return worksheet names and their corresponding vehicle identifiers
     * @throws IOException if the workbook cannot be read
     */
    @NotNull
    List<SheetVehicleIdentifier> read(@NotNull Path workbook)
            throws IOException;
}