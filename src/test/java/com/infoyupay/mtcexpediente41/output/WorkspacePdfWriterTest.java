package com.infoyupay.mtcexpediente41.output;

import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysis;
import com.infoyupay.mtcexpediente41.analysis.WorkspaceGroup;
import com.infoyupay.mtcexpediente41.analysis.WorkspaceVehicle;
import com.infoyupay.mtcexpediente41.datasheet.SheetVehicleIdentifier;
import com.infoyupay.mtcexpediente41.pdf.GroupPdfDocument;
import com.infoyupay.mtcexpediente41.pdf.VehiclePdfDocument;
import com.infoyupay.mtcexpediente41.workspace.Workspace;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WorkspacePdfWriter}.
 *  <div data-infoyupay="manual-test-verification"
 *    style="border:1px solid #c00;
 *    border-radius:2px;
 *    padding:4px;
 *    color:#c00;
 *    margin-top:6px;
 *    margin-bottom:6px;">
 *       <strong>Tested-by:</strong>
 *       dvidal@infoyupay.com - passed 1 tests in 5.765s at 2026-07-27T13:29:49 (UTC-5).
 * </div>
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class WorkspacePdfWriterTest {

    @TempDir
    private Path tempDirectory;

    /**
     * Verifies versioned output names, individual document order, and global
     * request and technical-sheet consolidation order.
     *
     * @throws Exception if the PDF fixture or generated output cannot be
     *                   written or read
     */
    @Test
    void should_write_individual_and_consolidated_pdfs() throws Exception {
        var sourceDirectory = Files.createDirectories(
                tempDirectory.resolve("Source"));

        var groupOneBrochure = createPdf(
                sourceDirectory.resolve("1.1 Brochure.pdf"),
                101F);
        var groupOneRequest = createPdf(
                sourceDirectory.resolve("1.2 Request 1002.pdf"),
                102F);
        var groupOneTechnicalSheet = createPdf(
                sourceDirectory.resolve("1.3 Technical sheet 1002.pdf"),
                103F);
        var groupOneAdditionalDocument = createPdf(
                sourceDirectory.resolve("1.4 Additional 1002.pdf"),
                104F);

        var groupTwoBrochure = createPdf(
                sourceDirectory.resolve("2.1 Brochure.pdf"),
                201F);
        var groupTwoRequest = createPdf(
                sourceDirectory.resolve("2.2 Request 1001.pdf"),
                202F);
        var groupTwoTechnicalSheet = createPdf(
                sourceDirectory.resolve("2.3 Technical sheet 1001.pdf"),
                203F);

        var groupOneIdentifier = new SheetVehicleIdentifier(
                "Vehicle 2",
                "LZZ5BLND5SAJ01002");
        var groupTwoIdentifier = new SheetVehicleIdentifier(
                "Vehicle 1",
                "LZZ5BLND7SAJ01001");

        var groupOneVehicle = new WorkspaceVehicle(
                "1002",
                List.of(groupOneIdentifier),
                List.of(
                        vehicleDocument(1, 4, "1002",
                                groupOneAdditionalDocument),
                        vehicleDocument(1, 3, "1002",
                                groupOneTechnicalSheet),
                        vehicleDocument(1, 2, "1002",
                                groupOneRequest)));
        var groupTwoVehicle = new WorkspaceVehicle(
                "1001",
                List.of(groupTwoIdentifier),
                List.of(
                        vehicleDocument(2, 3, "1001",
                                groupTwoTechnicalSheet),
                        vehicleDocument(2, 2, "1001",
                                groupTwoRequest)));

        var workspace = workspace(sourceDirectory);
        var analysis = new WorkspaceAnalysis(
                workspace,
                List.of(groupOneIdentifier, groupTwoIdentifier),
                List.of(
                        new WorkspaceGroup(
                                2,
                                List.of(new GroupPdfDocument(
                                        2,
                                        groupTwoBrochure)),
                                List.of(groupTwoVehicle)),
                        new WorkspaceGroup(
                                1,
                                List.of(new GroupPdfDocument(
                                        1,
                                        groupOneBrochure)),
                                List.of(groupOneVehicle))),
                List.of(),
                List.of());

        var clock = Clock.fixed(
                Instant.parse("2026-07-27T23:45:00Z"),
                ZoneId.of("America/Lima"));

        var output = new WorkspacePdfWriter(clock).write(analysis);

        assertThat(output.individualFiles())
                .extracting(path -> path.getFileName().toString())
                .containsExactly(
                        "LZZ5BLND7SAJ01001 ver2026-07-27_18-45.pdf",
                        "LZZ5BLND5SAJ01002 ver2026-07-27_18-45.pdf");
        assertThat(output.consolidatedRequests().getFileName().toString())
                .isEqualTo(
                        "Solicitudes consolidadas ver2026-07-27_18-45.pdf");
        assertThat(output.consolidatedTechnicalSheets()
                .getFileName()
                .toString())
                .isEqualTo(
                        "Fichas técnicas consolidadas "
                                + "ver2026-07-27_18-45.pdf");

        assertPageWidths(output.individualFiles().getFirst(),
                201F, 202F, 203F);
        assertPageWidths(output.individualFiles().getLast(),
                101F, 102F, 103F, 104F);
        assertPageWidths(output.consolidatedRequests(),
                102F, 202F);
        assertPageWidths(output.consolidatedTechnicalSheets(),
                103F, 203F);
    }

    /**
     * Creates a one-page PDF whose width identifies its source in assertions.
     *
     * @param path destination fixture path
     * @param pageWidth page width in points
     * @return created fixture path
     * @throws IOException if the fixture cannot be written
     */
    private static Path createPdf(
            Path path,
            float pageWidth) throws IOException {
        try (var document = new PDDocument()) {
            document.addPage(new PDPage(new PDRectangle(pageWidth, 100F)));
            document.save(path.toFile());
        }

        return path;
    }

    /**
     * Creates a vehicle PDF document value.
     *
     * @param group document group
     * @param documentNumber vehicle document number
     * @param marker vehicle marker
     * @param path source PDF path
     * @return vehicle PDF document
     */
    private static VehiclePdfDocument vehicleDocument(
            int group,
            int documentNumber,
            String marker,
            Path path) {
        return new VehiclePdfDocument(
                group,
                documentNumber,
                marker,
                path);
    }

    /**
     * Creates a workspace value for the temporary fixture tree.
     *
     * @param sourceDirectory source directory containing the fixture files
     * @return test workspace
     */
    private Workspace workspace(Path sourceDirectory) {
        var outputDirectory = tempDirectory.resolve("Expedientes");

        return new Workspace(
                tempDirectory,
                sourceDirectory.resolve("Vehicles.xlsx"),
                List.of(),
                outputDirectory,
                outputDirectory.resolve("Individuales"),
                outputDirectory.resolve("Consolidados"));
    }

    /**
     * Verifies the page-width sequence of one generated PDF.
     *
     * @param path generated PDF path
     * @param expectedWidths expected page widths
     * @throws IOException if the generated PDF cannot be read
     */
    private static void assertPageWidths(
            Path path,
            Float... expectedWidths) throws IOException {
        try (var document = Loader.loadPDF(path.toFile())) {
            List<Float> actualWidths = new ArrayList<>();
            for (var page : document.getPages()) {
                actualWidths.add(page.getMediaBox().getWidth());
            }

            assertThat(actualWidths).containsExactly(expectedWidths);
        }
    }
}
