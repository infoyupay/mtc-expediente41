package com.infoyupay.mtcexpediente41.workspace;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Represents the physical files and output directories associated with a
 * document preparation workspace.
 *
 * @param rootDirectory               root directory selected by the user
 * @param workbook                    spreadsheet workbook found in the workspace
 * @param pdfFiles                    source PDF files found in the workspace
 * @param outputDirectory             root directory for generated documents
 * @param individualOutputDirectory   directory for individual expedients
 * @param consolidatedOutputDirectory directory for consolidated documents
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record Workspace(
        @NotNull Path rootDirectory,
        @NotNull Path workbook,
        @NotNull List<@NotNull Path> pdfFiles,
        @NotNull Path outputDirectory,
        @NotNull Path individualOutputDirectory,
        @NotNull Path consolidatedOutputDirectory
) {

    /**
     * Creates an immutable workspace description.
     *
     * @param rootDirectory               root directory selected by the user
     * @param workbook                    spreadsheet workbook found in the workspace
     * @param pdfFiles                    source PDF files found in the workspace
     * @param outputDirectory             root directory for generated documents
     * @param individualOutputDirectory   directory for individual expedients
     * @param consolidatedOutputDirectory directory for consolidated documents
     */
    public Workspace {
        Objects.requireNonNull(rootDirectory, "rootDirectory");
        Objects.requireNonNull(workbook, "workbook");
        Objects.requireNonNull(pdfFiles, "pdfFiles");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(
                individualOutputDirectory,
                "individualOutputDirectory");
        Objects.requireNonNull(
                consolidatedOutputDirectory,
                "consolidatedOutputDirectory");

        pdfFiles = List.copyOf(pdfFiles);
    }
}
