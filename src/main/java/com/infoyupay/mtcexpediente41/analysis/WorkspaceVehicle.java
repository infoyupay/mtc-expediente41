package com.infoyupay.mtcexpediente41.analysis;

import com.infoyupay.mtcexpediente41.datasheet.SheetVehicleIdentifier;
import com.infoyupay.mtcexpediente41.pdf.VehiclePdfDocument;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the vehicle-specific documents found for one marker within a
 * document group.
 * <br/>
 * A normal vehicle has exactly one workbook identifier. An empty identifier
 * list exposes a PDF marker missing from the workbook, while multiple entries
 * preserve an ambiguous marker instead of selecting a vehicle arbitrarily.
 *
 * @param marker four-character alphanumeric vehicle marker
 * @param identifiers workbook vehicles that share the marker
 * @param documents vehicle-specific PDF documents in document-number order
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record WorkspaceVehicle(
        @NotNull String marker,
        @NotNull List<@NotNull SheetVehicleIdentifier> identifiers,
        @NotNull List<@NotNull VehiclePdfDocument> documents
) {

    /**
     * Creates an immutable analyzed vehicle.
     *
     * @param marker four-character alphanumeric vehicle marker
     * @param identifiers workbook vehicles that share the marker
     * @param documents vehicle-specific PDF documents
     */
    public WorkspaceVehicle {
        Objects.requireNonNull(marker, "marker");
        Objects.requireNonNull(identifiers, "identifiers");
        Objects.requireNonNull(documents, "documents");

        identifiers = List.copyOf(identifiers);
        documents = List.copyOf(documents);

        if (marker.length() != 4
                || marker.chars().anyMatch(
                character -> !isAsciiAlphanumeric(character))) {
            throw new IllegalArgumentException(
                    "Vehicle marker must contain exactly four alphanumeric "
                            + "characters.");
        }

        if (documents.isEmpty()) {
            throw new IllegalArgumentException(
                    "Analyzed vehicle must contain at least one PDF document.");
        }

        if (documents.stream()
                .anyMatch(document -> !marker.equals(document.marker()))) {
            throw new IllegalArgumentException(
                    "Every vehicle document must use the analyzed marker.");
        }
    }

    /**
     * Determines whether a character belongs to the supported marker alphabet.
     *
     * @param character character to test
     * @return {@code true} for an ASCII letter or digit
     */
    private static boolean isAsciiAlphanumeric(int character) {
        return character >= '0' && character <= '9'
                || character >= 'A' && character <= 'Z'
                || character >= 'a' && character <= 'z';
    }

    /**
     * Returns the uniquely correlated workbook vehicle when the marker is
     * unambiguous.
     *
     * @return uniquely correlated workbook vehicle, or an empty optional
     */
    public @NotNull Optional<SheetVehicleIdentifier> identifier() {
        return identifiers.size() == 1
                ? Optional.of(identifiers.getFirst())
                : Optional.empty();
    }
}
