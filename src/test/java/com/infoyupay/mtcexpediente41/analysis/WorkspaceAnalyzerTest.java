package com.infoyupay.mtcexpediente41.analysis;

import com.infoyupay.mtcexpediente41.datasheet.SheetVehicleIdentifier;
import com.infoyupay.mtcexpediente41.pdf.GroupPdfDocument;
import com.infoyupay.mtcexpediente41.pdf.PdfDocument;
import com.infoyupay.mtcexpediente41.pdf.VehiclePdfDocument;
import com.infoyupay.mtcexpediente41.workspace.Workspace;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.AMBIGUOUS_VEHICLE_MARKER;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.DUPLICATE_GROUP_BROCHURE;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.DUPLICATE_VEHICLE_DOCUMENT;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.GROUP_WITHOUT_VEHICLES;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.INVALID_VEHICLE_MARKER;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.MISSING_GROUP_BROCHURE;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.UNKNOWN_VEHICLE_MARKER;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.VEHICLE_IN_MULTIPLE_GROUPS;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.VEHICLE_WITHOUT_DOCUMENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link WorkspaceAnalyzer}.
 *  <div data-infoyupay="manual-test-verification"
 *    style="border:1px solid #c00;
 *    border-radius:2px;
 *    padding:4px;
 *    color:#c00;
 *    margin-top:6px;
 *    margin-bottom:6px;">
 *       <strong>Tested-by:</strong>
 *       dvidal@infoyupay.com - passed 3 tests in 3.249s at 2026-07-27T10:01:12 (UTC-5).
 * </div>
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class WorkspaceAnalyzerTest {

    /**
     * Analyzer under test.
     */
    private final WorkspaceAnalyzer analyzer = new WorkspaceAnalyzer();

    /**
     * Verifies successful correlation, workbook vehicle ordering, and vehicle
     * document ordering.
     */
    @Test
    void should_correlate_a_consistent_workspace() {
        var workspace = workspace();
        var firstVehicle = new SheetVehicleIdentifier(
                "Vehicle B",
                "LZZ5BLND5SAJ01002");
        var secondVehicle = new SheetVehicleIdentifier(
                "Vehicle A",
                "LZZ5BLND7SAJ01001");

        List<PdfDocument> documents = List.of(
                vehicleDocument(1, 3, "1001"),
                vehicleDocument(1, 2, "1001"),
                vehicleDocument(1, 2, "1002"),
                brochure(1));

        var result = analyzer.analyze(
                workspace,
                List.of(firstVehicle, secondVehicle),
                documents);

        assertThat(result.workspace()).isSameAs(workspace);
        assertThat(result.groupCount()).isEqualTo(1);
        assertThat(result.vehicleCount()).isEqualTo(2);
        assertThat(result.canProceed()).isTrue();
        assertThat(result.ungroupedVehicleIdentifiers()).isEmpty();

        assertThat(result.groups()).singleElement().satisfies(group -> {
            assertThat(group.group()).isEqualTo(1);
            assertThat(group.brochure()).contains(brochure(1));
            assertThat(group.vehicles())
                    .extracting(WorkspaceVehicle::marker)
                    .containsExactly("1002", "1001");
            assertThat(group.vehicles().getFirst().identifier())
                    .contains(firstVehicle);
            assertThat(group.vehicles().getLast().documents())
                    .extracting(VehiclePdfDocument::documentNumber)
                    .containsExactly(2, 3);
        });
    }

    /**
     * Verifies that incomplete and ambiguous source data remains represented
     * while every blocking problem is reported.
     */
    @Test
    void should_preserve_inconsistent_sources_and_report_problems() {
        var firstAmbiguousVehicle = new SheetVehicleIdentifier(
                "Ambiguous A",
                "LZZ5BLND7SAJ01001");
        var secondAmbiguousVehicle = new SheetVehicleIdentifier(
                "Ambiguous B",
                "OTHER000000001001");
        var invalidVehicle = new SheetVehicleIdentifier(
                "Invalid",
                "INVALID-VIN");
        var missingVehicle = new SheetVehicleIdentifier(
                "Missing",
                "LZZ5BLND5SAJ02002");

        List<PdfDocument> documents = List.of(
                brochure(1),
                new GroupPdfDocument(1, Path.of("1.1 Duplicate.pdf")),
                vehicleDocument(1, 2, "1001"),
                new VehiclePdfDocument(
                        1,
                        2,
                        "1001",
                        Path.of("1.2 Duplicate 1001.pdf")),
                vehicleDocument(1, 3, "9999"),
                vehicleDocument(2, 3, "1001"),
                brochure(3));

        var result = analyzer.analyze(
                workspace(),
                List.of(
                        firstAmbiguousVehicle,
                        secondAmbiguousVehicle,
                        invalidVehicle,
                        missingVehicle),
                documents);

        assertThat(result.canProceed()).isFalse();
        assertThat(result.groups())
                .extracting(WorkspaceGroup::group)
                .containsExactly(1, 2, 3);
        assertThat(result.groups().getFirst().vehicles())
                .filteredOn(vehicle -> "1001".equals(vehicle.marker()))
                .singleElement()
                .extracting(vehicle -> vehicle.identifiers().size())
                .isEqualTo(2);
        assertThat(result.groups().getFirst().vehicles())
                .filteredOn(vehicle -> "9999".equals(vehicle.marker()))
                .singleElement()
                .extracting(WorkspaceVehicle::identifiers)
                .satisfies(identifiers -> assertThat(identifiers).isEmpty());
        assertThat(result.ungroupedVehicleIdentifiers())
                .containsExactly(invalidVehicle, missingVehicle);

        assertThat(result.problems())
                .extracting(WorkspaceAnalysisProblem::type)
                .containsExactlyInAnyOrder(
                        INVALID_VEHICLE_MARKER,
                        AMBIGUOUS_VEHICLE_MARKER,
                        VEHICLE_IN_MULTIPLE_GROUPS,
                        DUPLICATE_GROUP_BROCHURE,
                        DUPLICATE_VEHICLE_DOCUMENT,
                        UNKNOWN_VEHICLE_MARKER,
                        MISSING_GROUP_BROCHURE,
                        GROUP_WITHOUT_VEHICLES,
                        VEHICLE_WITHOUT_DOCUMENTS);
    }

    /**
     * Verifies that every list exposed by the result hierarchy is immutable.
     */
    @Test
    void should_return_immutable_analysis_collections() {
        var result = analyzer.analyze(
                workspace(),
                List.of(new SheetVehicleIdentifier(
                        "Vehicle",
                        "LZZ5BLND7SAJ01001")),
                List.of(
                        brochure(1),
                        vehicleDocument(1, 2, "1001")));

        assertThatThrownBy(result.vehicleIdentifiers()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(result.groups()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(result.ungroupedVehicleIdentifiers()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(result.problems()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(
                result.groups().getFirst().brochures()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(
                result.groups().getFirst().vehicles()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(result.groups()
                .getFirst()
                .vehicles()
                .getFirst()
                .identifiers()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(result.groups()
                .getFirst()
                .vehicles()
                .getFirst()
                .documents()::clear)
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Creates a group brochure test document.
     *
     * @param group document group
     * @return brochure document
     */
    private static GroupPdfDocument brochure(int group) {
        return new GroupPdfDocument(
                group,
                Path.of("%d.1 Brochure.pdf".formatted(group)));
    }

    /**
     * Creates a vehicle test document.
     *
     * @param group document group
     * @param documentNumber vehicle document number
     * @param marker vehicle marker
     * @return vehicle document
     */
    private static VehiclePdfDocument vehicleDocument(
            int group,
            int documentNumber,
            String marker) {
        return new VehiclePdfDocument(
                group,
                documentNumber,
                marker,
                Path.of("%d.%d Document %s.pdf".formatted(
                        group,
                        documentNumber,
                        marker)));
    }

    /**
     * Creates a test workspace without accessing the file system.
     *
     * @return test workspace
     */
    private static Workspace workspace() {
        var root = Path.of("workspace");

        return new Workspace(
                root,
                root.resolve("vehicles.xlsx"),
                List.of(),
                root.resolve("Expedientes"),
                root.resolve("Expedientes/Individuales"),
                root.resolve("Expedientes/Consolidados"));
    }
}
