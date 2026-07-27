package com.infoyupay.mtcexpediente41.javafx.task;

import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysis;
import com.infoyupay.mtcexpediente41.analysis.WorkspaceGroup;
import com.infoyupay.mtcexpediente41.analysis.WorkspaceVehicle;
import com.infoyupay.mtcexpediente41.datasheet.SheetVehicleIdentifier;
import com.infoyupay.mtcexpediente41.pdf.GroupPdfDocument;
import com.infoyupay.mtcexpediente41.pdf.VehiclePdfDocument;
import com.infoyupay.mtcexpediente41.workspace.Workspace;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GenerateWorkspaceTask}.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class GenerateWorkspaceTaskTest {

    @TempDir
    private Path tempDirectory;

    /**
     * Verifies that the task delegates workspace generation and returns every
     * generated output path.
     *
     * @throws Exception if a source or generated PDF cannot be written
     */
    @Test
    void should_generate_workspace_pdf_output() throws Exception {
        var sourceDirectory = Files.createDirectories(
                tempDirectory.resolve("Source"));
        var brochure = createPdf(
                sourceDirectory.resolve("1.1 Brochure.pdf"));
        var request = createPdf(
                sourceDirectory.resolve("1.2 Request 1001.pdf"));
        var technicalSheet = createPdf(
                sourceDirectory.resolve("1.3 Technical sheet 1001.pdf"));

        var identifier = new SheetVehicleIdentifier(
                "Vehicle 1",
                "LZZ5BLND7SAJ01001");
        var vehicle = new WorkspaceVehicle(
                "1001",
                List.of(identifier),
                List.of(
                        new VehiclePdfDocument(1, 2, "1001", request),
                        new VehiclePdfDocument(
                                1,
                                3,
                                "1001",
                                technicalSheet)));
        var outputDirectory = tempDirectory.resolve("Expedientes");
        var workspace = new Workspace(
                tempDirectory,
                sourceDirectory.resolve("Vehicles.xlsx"),
                List.of(brochure, request, technicalSheet),
                outputDirectory,
                outputDirectory.resolve("Individuales"),
                outputDirectory.resolve("Consolidados"));
        var analysis = new WorkspaceAnalysis(
                workspace,
                List.of(identifier),
                List.of(new WorkspaceGroup(
                        1,
                        List.of(new GroupPdfDocument(1, brochure)),
                        List.of(vehicle))),
                List.of(),
                List.of());

        var output = new GenerateWorkspaceTask(analysis).call();

        assertThat(output.individualFiles())
                .singleElement()
                .satisfies(path -> assertThat(path).isRegularFile());
        assertThat(output.consolidatedRequests()).isRegularFile();
        assertThat(output.consolidatedTechnicalSheets()).isRegularFile();
    }

    /**
     * Creates a valid one-page PDF source.
     *
     * @param path destination source path
     * @return created source path
     * @throws IOException if the PDF cannot be written
     */
    private static Path createPdf(Path path) throws IOException {
        try (var document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(path.toFile());
        }
        return path;
    }
}
