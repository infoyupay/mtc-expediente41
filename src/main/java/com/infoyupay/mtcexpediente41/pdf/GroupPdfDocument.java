package com.infoyupay.mtcexpediente41.pdf;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a brochure shared by every vehicle in one document group.
 *
 * @param group document group
 * @param path source PDF path
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record GroupPdfDocument(
        int group,
        @NotNull Path path
) implements PdfDocument {

    /**
     * Creates a group-scoped brochure document.
     *
     * @param group document group
     * @param path source PDF path
     */
    public GroupPdfDocument {
        if (group < 1) {
            throw new IllegalArgumentException(
                    "Document group must be positive.");
        }

        Objects.requireNonNull(path, "path");
    }

    /**
     * Returns the brochure document number.
     *
     * @return {@code 1}
     */
    @Override
    public int documentNumber() {
        return 1;
    }
}
