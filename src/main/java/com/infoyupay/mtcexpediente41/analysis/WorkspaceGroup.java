package com.infoyupay.mtcexpediente41.analysis;

import com.infoyupay.mtcexpediente41.pdf.GroupPdfDocument;
import com.infoyupay.mtcexpediente41.pdf.PdfDocument;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
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
 * @param commonDocuments group-scoped shared documents
 * @param vehicles vehicles represented by documents in this group
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record WorkspaceGroup(
        int group,
        @NotNull List<@NotNull GroupPdfDocument> commonDocuments,
        @NotNull List<@NotNull WorkspaceVehicle> vehicles
) {

    /**
     * Creates an immutable analyzed document group.
     *
     * @param group positive document group
     * @param commonDocuments group-scoped shared documents
     * @param vehicles vehicles represented by documents in this group
     */
    public WorkspaceGroup {
        Objects.requireNonNull(commonDocuments, "commonDocuments");
        Objects.requireNonNull(vehicles, "vehicles");

        commonDocuments = List.copyOf(commonDocuments);
        vehicles = List.copyOf(vehicles);

        if (group < 1) {
            throw new IllegalArgumentException(
                    "Document group must be positive.");
        }

        if (commonDocuments.stream()
                .anyMatch(document -> document.group() != group)) {
            throw new IllegalArgumentException(
                    "Every shared document must belong to the analyzed group.");
        }

        if (vehicles.stream()
                .flatMap(vehicle -> vehicle.documents().stream())
                .anyMatch(document -> document.group() != group)) {
            throw new IllegalArgumentException(
                    "Every vehicle document must belong to the analyzed group.");
        }
    }

    /** Returns the shared documents at the brochure position. */
    public @NotNull List<GroupPdfDocument> brochures() {
        return commonDocuments.stream()
                .filter(document -> document.documentNumber() == 1)
                .toList();
    }

    /** Returns the ordered sources for one individual expediente. */
    public @NotNull List<PdfDocument> documentsFor(
            @NotNull WorkspaceVehicle vehicle) {
        List<PdfDocument> documents =
                new ArrayList<>(commonDocuments);
        documents.addAll(vehicle.documents());
        documents.sort(Comparator.comparingInt(
                PdfDocument::documentNumber));
        return List.copyOf(documents);
    }

    /**
     * Returns the brochure when the group contains exactly one.
     *
     * @return unique group brochure, or an empty optional
     */
    public @NotNull Optional<GroupPdfDocument> brochure() {
        var brochures = brochures();
        return brochures.size() == 1
                ? Optional.of(brochures.getFirst())
                : Optional.empty();
    }
}
