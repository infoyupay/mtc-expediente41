package com.infoyupay.mtcexpediente41.analysis;

import com.infoyupay.mtcexpediente41.pdf.GroupPdfDocument;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents one document group discovered in an analyzed workspace.
 * <br/>
 * Brochures are retained as a list so missing and duplicate group documents
 * remain visible to callers. Vehicles are ordered according to their workbook
 * position, with unknown markers placed last.
 *
 * @param group positive document group
 * @param brochures group-scoped brochure documents
 * @param vehicles vehicles represented by documents in this group
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record WorkspaceGroup(
        int group,
        @NotNull List<@NotNull GroupPdfDocument> brochures,
        @NotNull List<@NotNull WorkspaceVehicle> vehicles
) {

    /**
     * Creates an immutable analyzed document group.
     *
     * @param group positive document group
     * @param brochures group-scoped brochure documents
     * @param vehicles vehicles represented by documents in this group
     */
    public WorkspaceGroup {
        Objects.requireNonNull(brochures, "brochures");
        Objects.requireNonNull(vehicles, "vehicles");

        brochures = List.copyOf(brochures);
        vehicles = List.copyOf(vehicles);

        if (group < 1) {
            throw new IllegalArgumentException(
                    "Document group must be positive.");
        }

        if (brochures.stream()
                .anyMatch(document -> document.group() != group)) {
            throw new IllegalArgumentException(
                    "Every brochure must belong to the analyzed group.");
        }

        if (vehicles.stream()
                .flatMap(vehicle -> vehicle.documents().stream())
                .anyMatch(document -> document.group() != group)) {
            throw new IllegalArgumentException(
                    "Every vehicle document must belong to the analyzed group.");
        }
    }

    /**
     * Returns the brochure when the group contains exactly one.
     *
     * @return unique group brochure, or an empty optional
     */
    public @NotNull Optional<GroupPdfDocument> brochure() {
        return brochures.size() == 1
                ? Optional.of(brochures.getFirst())
                : Optional.empty();
    }
}
