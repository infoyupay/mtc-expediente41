package com.infoyupay.mtcexpediente41.pdf;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides operations over parsed source PDF documents.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class PdfDocuments {

    /**
     * Prevents instantiation of this utility class.
     */
    private PdfDocuments() {
    }

    /**
     * Selects vehicle documents of one document number and orders them by group
     * and marker for consolidation into one PDF spanning the whole batch.
     *
     * @param documents parsed PDF documents
     * @param documentNumber vehicle document number to consolidate
     * @return immutable consolidation sequence
     * @throws IllegalArgumentException if the document number belongs to the
     *                                  group brochure
     */
    public static @NotNull List<@NotNull VehiclePdfDocument> forConsolidation(
            @NotNull Iterable<@NotNull PdfDocument> documents,
            int documentNumber) {
        Objects.requireNonNull(documents, "documents");

        if (documentNumber < 2) {
            throw new IllegalArgumentException(
                    "Consolidation requires a vehicle document number.");
        }

        List<VehiclePdfDocument> selectedDocuments = new ArrayList<>();
        for (var document : documents) {
            if (document instanceof VehiclePdfDocument vehicleDocument
                    && vehicleDocument.documentNumber() == documentNumber) {
                selectedDocuments.add(vehicleDocument);
            }
        }

        selectedDocuments.sort(VehiclePdfDocument.BY_GROUP_AND_MARKER);
        return List.copyOf(selectedDocuments);
    }
}
