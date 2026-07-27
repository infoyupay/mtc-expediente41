package com.infoyupay.mtcexpediente41.javafx.task;

import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysis;
import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalyzer;
import com.infoyupay.mtcexpediente41.datasheet.OdsVehicleIdentifierReader;
import com.infoyupay.mtcexpediente41.datasheet.VehicleIdentifierReader;
import com.infoyupay.mtcexpediente41.datasheet.XlsxVehicleIdentifierReader;
import com.infoyupay.mtcexpediente41.pdf.PdfDocumentNameException;
import com.infoyupay.mtcexpediente41.pdf.PdfDocumentParser;
import com.infoyupay.mtcexpediente41.workspace.WorkspaceException;
import com.infoyupay.mtcexpediente41.workspace.WorkspaceScanner;
import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Reads and analyzes a workspace outside the JavaFX application thread.
 * <br/>
 * The task discovers the workspace files, reads the vehicle identifiers from
 * its spreadsheet workbook, parses the source PDF file names, and correlates
 * both inventories into one immutable {@link WorkspaceAnalysis}.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class ReadWorkspaceTask extends Task<WorkspaceAnalysis> {

    private final Path workspaceDirectory;

    /**
     * Creates a task for the selected workspace directory.
     *
     * @param workspaceDirectory root directory selected by the user
     */
    public ReadWorkspaceTask(@NotNull Path workspaceDirectory) {
        this.workspaceDirectory = Objects.requireNonNull(
                workspaceDirectory,
                "workspaceDirectory");
    }

    /**
     * Selects the vehicle-identifier reader for a supported workbook.
     *
     * @param workbook supported XLSX or ODS workbook
     * @return matching vehicle-identifier reader
     * @throws UnsupportedWorkbookFormatException if the workbook suffix is
     *                                            unsupported
     */
    private static VehicleIdentifierReader readerFor(Path workbook) {
        var originalName = workbook
                .getFileName()
                .toString();
        var lowerName = originalName.toLowerCase(Locale.ROOT);

        if (lowerName.endsWith(".xlsx")) {
            return new XlsxVehicleIdentifierReader();
        }

        if (lowerName.endsWith(".ods")) {
            return new OdsVehicleIdentifierReader();
        }

        throw new UnsupportedWorkbookFormatException(originalName);
    }

    /**
     * Reads every workspace source and produces its correlated analysis.
     *
     * @return immutable workspace analysis
     * @throws WorkspaceException       if the workspace cannot be discovered or
     *                                  prepared
     * @throws IOException              if the spreadsheet workbook cannot be read
     * @throws PdfDocumentNameException if a source PDF file name is invalid
     */
    @Override
    protected @NotNull WorkspaceAnalysis call()
            throws WorkspaceException,
            IOException,
            PdfDocumentNameException {
        var workspace = new WorkspaceScanner().scan(workspaceDirectory);
        var identifiers = readerFor(workspace.workbook())
                .read(workspace.workbook());
        var documents = new PdfDocumentParser()
                .parseAll(workspace.pdfFiles());

        return new WorkspaceAnalyzer().analyze(
                workspace,
                identifiers,
                documents);
    }
}
