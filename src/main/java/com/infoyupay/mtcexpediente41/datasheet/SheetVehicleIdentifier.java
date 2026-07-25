package com.infoyupay.mtcexpediente41.datasheet;

import org.jetbrains.annotations.NotNull;

/**
 * Associates an XLSX worksheet with the vehicle identifier rendered in its
 * canonical output cell.
 *
 * @param sheetName         worksheet display name
 * @param vehicleIdentifier value stored in cell C13
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record SheetVehicleIdentifier(
        @NotNull String sheetName,
        @NotNull String vehicleIdentifier
) {
}