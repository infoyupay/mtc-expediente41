package com.infoyupay.mtcexpediente41.javafx.task;

import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReadWorkspaceTask}.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class ReadWorkspaceTaskTest {

    /**
     * Vehicle markers contained in both sample workbooks.
     */
    private static final List<String> VEHICLE_MARKERS = List.of(
            "1001",
            "1002",
            "1003",
            "1004",
            "1005",
            "1006",
            "1007",
            "1008");

    /**
     * Verifies the complete workspace-reading pipeline for an ODS workbook.
     *
     * @param tempDirectory temporary workspace directory
     * @throws Exception if the workspace fixture cannot be prepared or read
     */
    @Test
    void should_read_and_analyze_ods_workspace(
            @TempDir Path tempDirectory) throws Exception {
        copyWorkbook(
                "/com/infoyupay/mtcexpediente41/pdf-samples/Fichas.ods",
                tempDirectory.resolve("Fichas.ods"));
        createPdfDocuments(tempDirectory);

        assertSuccessfulAnalysis(
                new ReadWorkspaceTask(tempDirectory).call(),
                "Fichas.ods");
    }

    /**
     * Verifies the complete workspace-reading pipeline for an XLSX workbook.
     *
     * @param tempDirectory temporary workspace directory
     * @throws Exception if the workspace fixture cannot be prepared or read
     */
    @Test
    void should_read_and_analyze_xlsx_workspace(
            @TempDir Path tempDirectory) throws Exception {
        copyWorkbook(
                "/com/infoyupay/mtcexpediente41/pdf-samples/Fichas.xlsx",
                tempDirectory.resolve("Fichas.xlsx"));
        createPdfDocuments(tempDirectory);

        assertSuccessfulAnalysis(
                new ReadWorkspaceTask(tempDirectory).call(),
                "Fichas.xlsx");
    }

    /**
     * Copies a sample workbook into a temporary workspace.
     *
     * @param resourceName classpath location of the sample workbook
     * @param target destination workbook path
     * @throws IOException if the workbook cannot be copied
     */
    private static void copyWorkbook(
            String resourceName,
            Path target) throws IOException {
        var input = Objects.requireNonNull(
                ReadWorkspaceTaskTest.class.getResourceAsStream(resourceName),
                "Test resource not found: " + resourceName);

        try (input) {
            Files.copy(input, target);
        }
    }

    /**
     * Creates one valid brochure and one vehicle document per sample marker.
     *
     * @param directory temporary workspace directory
     * @throws IOException if a source PDF fixture cannot be created
     */
    private static void createPdfDocuments(Path directory)
            throws IOException {
        Files.createFile(directory.resolve("1.1 Brochure.pdf"));

        for (var marker : VEHICLE_MARKERS) {
            Files.createFile(directory.resolve(
                    "1.2 Document %s.pdf".formatted(marker)));
        }
    }

    /**
     * Verifies the expected consistent workspace analysis.
     *
     * @param analysis completed workspace analysis
     * @param workbookFileName expected workbook file name
     */
    private static void assertSuccessfulAnalysis(
            WorkspaceAnalysis analysis,
            String workbookFileName) {
        assertThat(analysis.canProceed()).isTrue();
        assertThat(analysis.vehicleCount()).isEqualTo(VEHICLE_MARKERS.size());
        assertThat(analysis.groupCount()).isEqualTo(1);
        assertThat(analysis.workspace().workbook().getFileName().toString())
                .isEqualTo(workbookFileName);
        assertThat(analysis.groups())
                .singleElement()
                .satisfies(group -> assertThat(group.vehicles())
                        .hasSize(VEHICLE_MARKERS.size()));
    }
}
