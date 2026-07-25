package com.infoyupay.mtcexpediente41.workspace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link WorkspaceScanner}.
 *  <div data-infoyupay="manual-test-verification"
 *    style="border:1px solid #c00;
 *    border-radius:2px;
 *    padding:4px;
 *    color:#c00;
 *    margin-top:6px;
 *    margin-bottom:6px;">
 *       <strong>Tested-by:</strong>
 *       dvidal@infoyupay.com - passed 3 tests in 2.241s at 2026-07-25T00:34:14 (UTC-5).
 * </div>
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class WorkspaceScannerTest {

    @TempDir
    private Path tempDirectory;

    /**
     * Verifies recursive source discovery, output exclusion, and automatic
     * preparation of the output directory tree.
     *
     * @throws Exception if the sample workspace cannot be prepared or scanned
     */
    @Test
    void shouldDiscoverAndPrepareWorkspace() throws Exception {
        var sourceDirectory = Files.createDirectories(
                tempDirectory.resolve("Source/Nested"));
        var workbook = Files.createFile(
                sourceDirectory.resolve("Fichas.XLSX"));
        var firstPdf = Files.createFile(
                tempDirectory.resolve("1.2 1001.pdf"));
        var secondPdf = Files.createFile(
                sourceDirectory.resolve("1.3 1001.PDF"));

        var previousOutputDirectory = Files.createDirectories(
                tempDirectory.resolve("Expedientes/Consolidados"));
        var generatedPdf = Files.createFile(
                previousOutputDirectory.resolve("Anterior.pdf"));

        var workspace = new WorkspaceScanner().scan(tempDirectory);

        assertThat(workspace.rootDirectory())
                .isEqualTo(tempDirectory.toAbsolutePath().normalize());
        assertThat(workspace.workbook())
                .isEqualTo(workbook.toAbsolutePath().normalize());
        assertThat(workspace.pdfFiles())
                .containsExactlyInAnyOrder(
                        firstPdf.toAbsolutePath().normalize(),
                        secondPdf.toAbsolutePath().normalize())
                .doesNotContain(generatedPdf.toAbsolutePath().normalize());
        assertThat(workspace.outputDirectory())
                .isEqualTo(tempDirectory.resolve("Expedientes"));
        assertThat(workspace.individualOutputDirectory())
                .isEqualTo(tempDirectory.resolve("Expedientes/Individuales"))
                .isDirectory();
        assertThat(workspace.consolidatedOutputDirectory())
                .isEqualTo(tempDirectory.resolve("Expedientes/Consolidados"))
                .isDirectory();
    }

    /**
     * Verifies that a workspace without a supported workbook is rejected
     * before output directories are created.
     */
    @Test
    void shouldRejectWorkspaceWithoutWorkbook() {
        var scanner = new WorkspaceScanner();

        assertThatThrownBy(() -> scanner.scan(tempDirectory))
                .isInstanceOf(WorkspaceException.class)
                .hasMessage(
                        "Workspace must contain exactly one XLSX or ODS "
                                + "workbook, but found 0.");
        assertThat(tempDirectory.resolve("Expedientes"))
                .doesNotExist();
    }

    /**
     * Verifies that ambiguous workspaces containing multiple supported
     * workbooks are rejected.
     *
     * @throws Exception if the sample files cannot be created
     */
    @Test
    void shouldRejectWorkspaceWithMultipleWorkbooks() throws Exception {
        Files.createFile(tempDirectory.resolve("Fichas.xlsx"));
        var nestedDirectory = Files.createDirectories(
                tempDirectory.resolve("Nested"));
        Files.createFile(nestedDirectory.resolve("Fichas.ods"));

        assertThatThrownBy(
                () -> new WorkspaceScanner().scan(tempDirectory))
                .isInstanceOf(WorkspaceException.class)
                .hasMessage(
                        "Workspace must contain exactly one XLSX or ODS "
                                + "workbook, but found 2.");
    }
}
