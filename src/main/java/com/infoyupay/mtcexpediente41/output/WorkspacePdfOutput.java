package com.infoyupay.mtcexpediente41.output;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Describes the PDF files generated for one workspace.
 *
 * @param individualFiles individual vehicle expedient PDFs
 * @param consolidatedRequests lot-wide request consolidation
 * @param consolidatedTechnicalSheets lot-wide technical-sheet consolidation
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record WorkspacePdfOutput(
        @NotNull List<@NotNull Path> individualFiles,
        @NotNull Path consolidatedRequests,
        @NotNull Path consolidatedTechnicalSheets
) {

    /**
     * Creates an immutable workspace PDF output description.
     *
     * @param individualFiles individual vehicle expedient PDFs
     * @param consolidatedRequests lot-wide request consolidation
     * @param consolidatedTechnicalSheets lot-wide technical-sheet consolidation
     */
    public WorkspacePdfOutput {
        Objects.requireNonNull(individualFiles, "individualFiles");
        Objects.requireNonNull(
                consolidatedRequests,
                "consolidatedRequests");
        Objects.requireNonNull(
                consolidatedTechnicalSheets,
                "consolidatedTechnicalSheets");

        individualFiles = List.copyOf(individualFiles);
    }
}
