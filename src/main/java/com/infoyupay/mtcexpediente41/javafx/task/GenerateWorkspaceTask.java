package com.infoyupay.mtcexpediente41.javafx.task;

import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysis;
import com.infoyupay.mtcexpediente41.output.WorkspacePdfOutput;
import com.infoyupay.mtcexpediente41.output.WorkspacePdfWriter;
import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * Generates the PDF output of an analyzed workspace outside the JavaFX
 * application thread.
 * <br/>
 * The task delegates the low-level output operation to
 * {@link WorkspacePdfWriter} and exposes the resulting
 * {@link WorkspacePdfOutput} through the JavaFX worker lifecycle.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class GenerateWorkspaceTask extends Task<WorkspacePdfOutput> {

    private final WorkspaceAnalysis analysis;

    /**
     * Creates a task for a completed workspace analysis.
     *
     * @param analysis workspace analysis to generate
     */
    public GenerateWorkspaceTask(@NotNull WorkspaceAnalysis analysis) {
        this.analysis = Objects.requireNonNull(analysis, "analysis");
    }

    /**
     * Generates every individual and consolidated workspace PDF.
     *
     * @return immutable description of the generated PDF files
     * @throws IOException if a source cannot be read or an output cannot be
     *                     written
     */
    @Override
    protected @NotNull WorkspacePdfOutput call() throws IOException {
        updateMessage("Iniciando generación de expedientes PDF...");
        var output = new WorkspacePdfWriter().write(analysis);
        updateMessage(
                "Se generaron %d expedientes individuales y 2 consolidados."
                        .formatted(output.individualFiles().size()));
        return output;
    }
}
