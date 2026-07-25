package com.infoyupay.mtcexpediente41.pdf;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Represents a source PDF document identified from its deterministic file name.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public sealed interface PdfDocument
        permits GroupPdfDocument, VehiclePdfDocument {

    /**
     * Returns the document group encoded at the beginning of the file name.
     *
     * @return positive document group
     */
    int group();

    /**
     * Returns the document number encoded after the group.
     *
     * @return positive document number
     */
    int documentNumber();

    /**
     * Returns the source PDF path.
     *
     * @return source PDF path
     */
    @NotNull Path path();
}
