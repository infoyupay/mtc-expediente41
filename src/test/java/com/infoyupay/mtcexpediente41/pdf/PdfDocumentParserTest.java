package com.infoyupay.mtcexpediente41.pdf;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Tests for {@link PdfDocumentParser} and {@link PdfDocuments}.
 *  <div data-infoyupay="manual-test-verification"
 *    style="border:1px solid #c00;
 *    border-radius:2px;
 *    padding:4px;
 *    color:#c00;
 *    margin-top:6px;
 *    margin-bottom:6px;">
 *       <strong>Tested-by:</strong>
 *       dvidal@infoyupay.com - passed 8 tests in 2.228s at 2026-07-25T01:16:32 (UTC-5).
 * </div>
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class PdfDocumentParserTest {

    private static final String SAMPLE_DIRECTORY =
            "/com/infoyupay/mtcexpediente41/pdf-samples";

    private final PdfDocumentParser parser = new PdfDocumentParser();

    /**
     * Verifies that document number one is parsed as a group brochure without
     * treating a trailing year as a vehicle marker.
     *
     * @throws PdfDocumentNameException if the sample name cannot be parsed
     */
    @Test
    void shouldParseGroupBrochureWithoutMarker()
            throws PdfDocumentNameException {
        var path = Path.of("01.01 Brochure Model 2024.pdf");

        var document = parser.parse(path);

        assertThat(document)
                .isInstanceOfSatisfying(
                        GroupPdfDocument.class,
                        brochure -> {
                            assertThat(brochure.group()).isEqualTo(1);
                            assertThat(brochure.documentNumber()).isEqualTo(1);
                            assertThat(brochure.path()).isEqualTo(path);
                        });
    }

    /**
     * Verifies that zero-padded group and document segments provide their
     * numeric values together with the vehicle marker.
     *
     * @throws PdfDocumentNameException if the sample name cannot be parsed
     */
    @Test
    void shouldParseZeroPaddedVehicleDocument()
            throws PdfDocumentNameException {
        var path = Path.of("02.04 Fotos A1B2.pdf");

        var document = parser.parse(path);

        assertThat(document)
                .isInstanceOfSatisfying(
                        VehiclePdfDocument.class,
                        vehicleDocument -> {
                            assertThat(vehicleDocument.group()).isEqualTo(2);
                            assertThat(vehicleDocument.documentNumber())
                                    .isEqualTo(4);
                            assertThat(vehicleDocument.marker())
                                    .isEqualTo("A1B2");
                            assertThat(vehicleDocument.path()).isEqualTo(path);
                        });
    }

    /**
     * Verifies that a vehicle document without descriptive text and with an
     * upper-case PDF suffix is accepted.
     *
     * @throws PdfDocumentNameException if the sample name cannot be parsed
     */
    @Test
    void shouldParseVehicleDocumentWithoutDescription()
            throws PdfDocumentNameException {
        var document = parser.parse(Path.of("1.3 1001.PDF"));

        assertThat(document)
                .isInstanceOfSatisfying(
                        VehiclePdfDocument.class,
                        vehicleDocument -> {
                            assertThat(vehicleDocument.group()).isEqualTo(1);
                            assertThat(vehicleDocument.documentNumber())
                                    .isEqualTo(3);
                            assertThat(vehicleDocument.marker())
                                    .isEqualTo("1001");
                        });
    }

    /**
     * Verifies that vehicle documents without a trailing marker are rejected.
     */
    @Test
    void shouldRejectVehicleDocumentWithoutMarker() {
        var path = Path.of("1.2 Special.pdf");

        assertThatExceptionOfType(PdfDocumentNameException.class)
                .isThrownBy(() -> parser.parse(path))
                .withMessageContaining(
                        "four-character alphanumeric marker");
    }

    /**
     * Verifies that every source PDF bundled with the sample workbooks follows
     * the supported naming convention.
     *
     * @throws Exception if the sample directory cannot be resolved or read
     */
    @Test
    void shouldParseEverySamplePdf() throws Exception {
        var documents = readSampleDocuments();

        assertThat(documents).hasSize(23);
        assertThat(documents)
                .filteredOn(GroupPdfDocument.class::isInstance)
                .hasSize(3);
        assertThat(documents)
                .filteredOn(VehiclePdfDocument.class::isInstance)
                .hasSize(20);
    }

    /**
     * Verifies the global group-then-marker order used for the consolidated
     * special-characteristics document.
     *
     * @throws Exception if the sample directory cannot be resolved or read
     */
    @Test
    void shouldOrderSpecialCharacteristicsForConsolidation()
            throws Exception {
        var orderedDocuments = PdfDocuments.forConsolidation(
                readSampleDocuments(),
                2);

        assertExpectedVehicleOrder(orderedDocuments);
    }

    /**
     * Verifies the global group-then-marker order used for the consolidated
     * technical-datasheet document.
     *
     * @throws Exception if the sample directory cannot be resolved or read
     */
    @Test
    void shouldOrderTechnicalDatasheetsForConsolidation()
            throws Exception {
        var orderedDocuments = PdfDocuments.forConsolidation(
                readSampleDocuments(),
                3);

        assertExpectedVehicleOrder(orderedDocuments);
    }

    /**
     * Verifies that group brochures cannot be selected as vehicle
     * consolidations.
     */
    @Test
    void shouldRejectBrochureConsolidationSelection() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PdfDocuments.forConsolidation(List.of(), 1))
                .withMessageContaining("vehicle document number");
    }

    /**
     * Reads and parses all PDF resources in the sample directory.
     *
     * @return parsed sample documents
     * @throws Exception if the sample directory cannot be resolved or read
     */
    private List<PdfDocument> readSampleDocuments() throws Exception {
        var resource = getClass().getResource(SAMPLE_DIRECTORY);

        assertThat(resource)
                .as("Sample resource directory %s", SAMPLE_DIRECTORY)
                .isNotNull();

        try (var paths = Files.list(Path.of(resource.toURI()))) {
            var pdfPaths = paths
                    .filter(Files::isRegularFile)
                    .filter(PdfDocumentParserTest::isPdf)
                    .toList();

            return parser.parseAll(pdfPaths);
        }
    }

    /**
     * Verifies the marker sequence represented by the sample batch.
     *
     * @param documents ordered vehicle documents
     */
    private static void assertExpectedVehicleOrder(
            List<VehiclePdfDocument> documents) {
        assertThat(documents)
                .extracting(
                        VehiclePdfDocument::group,
                        VehiclePdfDocument::marker)
                .containsExactly(
                        tuple(1, "1001"),
                        tuple(1, "1002"),
                        tuple(1, "1003"),
                        tuple(1, "1004"),
                        tuple(1, "1005"),
                        tuple(2, "1006"),
                        tuple(2, "1007"),
                        tuple(3, "1008"));
    }

    /**
     * Determines whether a test resource path identifies a PDF file.
     *
     * @param path resource path
     * @return {@code true} for PDF resources
     */
    private static boolean isPdf(Path path) {
        return path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT)
                .endsWith(".pdf");
    }
}
