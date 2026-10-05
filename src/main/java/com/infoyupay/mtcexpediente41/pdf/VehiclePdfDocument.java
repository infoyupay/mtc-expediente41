package com.infoyupay.mtcexpediente41.pdf;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a vehicle-specific PDF document identified by a four-character
 * alphanumeric marker.
 *
 * @param group document group
 * @param documentNumber vehicle document number
 * @param marker four-character alphanumeric vehicle marker
 * @param path source PDF path
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record VehiclePdfDocument(
        int group,
        int documentNumber,
        @NotNull String marker,
        @NotNull Path path
) implements PdfDocument {

    private static final Pattern MARKER_PATTERN =
            Pattern.compile("[A-Za-z0-9]{4}");

    /**
     * Orders vehicle documents by group and then by marker, matching the
     * sequence required when producing one consolidated PDF for the whole
     * batch.
     */
    public static final Comparator<VehiclePdfDocument> BY_GROUP_AND_MARKER =
            Comparator.comparingInt(VehiclePdfDocument::group)
                    .thenComparing(VehiclePdfDocument::marker);

    /**
     * Creates a vehicle-scoped PDF document.
     *
     * @param group document group
     * @param documentNumber vehicle document number
     * @param marker four-character alphanumeric vehicle marker
     * @param path source PDF path
     */
    public VehiclePdfDocument {
        if (group < 1) {
            throw new IllegalArgumentException(
                    "Document group must be positive.");
        }

        if (documentNumber < 2) {
            throw new IllegalArgumentException(
                    "Vehicle document number must be greater than one.");
        }

        Objects.requireNonNull(marker, "marker");
        Objects.requireNonNull(path, "path");

        if (!MARKER_PATTERN.matcher(marker).matches()) {
            throw new IllegalArgumentException(
                    "Vehicle marker must contain exactly four alphanumeric "
                            + "characters.");
        }
    }
}
