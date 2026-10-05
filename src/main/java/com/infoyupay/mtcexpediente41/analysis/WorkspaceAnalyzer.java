package com.infoyupay.mtcexpediente41.analysis;

import com.infoyupay.mtcexpediente41.datasheet.SheetVehicleIdentifier;
import com.infoyupay.mtcexpediente41.pdf.GroupPdfDocument;
import com.infoyupay.mtcexpediente41.pdf.PdfDocument;
import com.infoyupay.mtcexpediente41.pdf.VehiclePdfDocument;
import com.infoyupay.mtcexpediente41.workspace.Workspace;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.AMBIGUOUS_VEHICLE_MARKER;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.DUPLICATE_GROUP_BROCHURE;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.DUPLICATE_VEHICLE_DOCUMENT;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.GROUP_WITHOUT_VEHICLES;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.INVALID_VEHICLE_MARKER;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.MISSING_GROUP_BROCHURE;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.UNKNOWN_VEHICLE_MARKER;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.VEHICLE_IN_MULTIPLE_GROUPS;
import static com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysisProblemType.VEHICLE_WITHOUT_DOCUMENTS;

/**
 * Correlates workbook vehicle identifiers with parsed source PDF documents.
 * <br/>
 * The analyzer performs no file access and has no JavaFX dependency. It
 * preserves incomplete and ambiguous source data in the returned hierarchy
 * while reporting the corresponding problems, allowing callers to display an
 * accurate workspace map before document generation.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class WorkspaceAnalyzer {

    /**
     * Number of trailing alphanumeric characters used to correlate workbook
     * identifiers with vehicle PDF file names.
     */
    private static final int MARKER_LENGTH = 4;

    /**
     * Sorts source documents deterministically when their semantic positions
     * are equal.
     */
    private static final Comparator<PdfDocument> BY_PATH =
            Comparator.comparing(document -> document.path().toString());

    /**
     * Correlates the workbook vehicle inventory and parsed PDF documents.
     *
     * @param workspace physical workspace that owns the source files
     * @param vehicleIdentifiers vehicle identifiers read from the workbook
     * @param documents source documents parsed from the workspace PDF files
     * @return immutable workspace analysis
     */
    public @NotNull WorkspaceAnalysis analyze(
            @NotNull Workspace workspace,
            @NotNull List<@NotNull SheetVehicleIdentifier> vehicleIdentifiers,
            @NotNull List<@NotNull PdfDocument> documents) {
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(vehicleIdentifiers, "vehicleIdentifiers");
        Objects.requireNonNull(documents, "documents");

        var identifiers = List.copyOf(vehicleIdentifiers);
        var sourceDocuments = List.copyOf(documents);
        List<WorkspaceAnalysisProblem> problems = new ArrayList<>();
        List<SheetVehicleIdentifier> ungroupedVehicles = new ArrayList<>();

        var identifiersByMarker = indexIdentifiers(
                identifiers,
                ungroupedVehicles,
                problems);

        reportAmbiguousMarkers(identifiersByMarker, problems);

        var vehicleDocuments = sourceDocuments.stream()
                .filter(VehiclePdfDocument.class::isInstance)
                .map(VehiclePdfDocument.class::cast)
                .toList();

        var groupsByMarker = indexGroupsByMarker(vehicleDocuments);
        reportMarkersInMultipleGroups(groupsByMarker, problems);

        var groups = analyzeGroups(
                sourceDocuments,
                identifiersByMarker,
                identifiers,
                problems);

        reportVehiclesWithoutDocuments(
                identifiers,
                groupsByMarker,
                ungroupedVehicles,
                problems);

        return new WorkspaceAnalysis(
                workspace,
                identifiers,
                groups,
                ungroupedVehicles,
                problems);
    }

    /**
     * Indexes workbook vehicles by their trailing four-character alphanumeric
     * markers.
     *
     * @param identifiers workbook vehicle inventory
     * @param ungroupedVehicles destination for identifiers with invalid markers
     * @param problems destination for detected problems
     * @return identifiers indexed by marker in workbook order
     */
    private static Map<String, List<SheetVehicleIdentifier>> indexIdentifiers(
            List<SheetVehicleIdentifier> identifiers,
            List<SheetVehicleIdentifier> ungroupedVehicles,
            List<WorkspaceAnalysisProblem> problems) {
        Map<String, List<SheetVehicleIdentifier>> result =
                new LinkedHashMap<>();

        for (var identifier : identifiers) {
            var marker = extractMarker(identifier.vehicleIdentifier());

            if (marker == null) {
                ungroupedVehicles.add(identifier);
                problems.add(problem(
                        INVALID_VEHICLE_MARKER,
                        ("Vehicle identifier '%s' from sheet '%s' does not end "
                                + "with a four-character alphanumeric marker.")
                                .formatted(
                                        identifier.vehicleIdentifier(),
                                        identifier.sheetName())));
                continue;
            }

            result.computeIfAbsent(marker, _ -> new ArrayList<>())
                    .add(identifier);
        }

        result.replaceAll((_, values) -> List.copyOf(values));
        return Collections.unmodifiableMap(new LinkedHashMap<>(result));
    }

    /**
     * Extracts a four-character alphanumeric marker from the end of a vehicle
     * identifier.
     *
     * @param vehicleIdentifier complete workbook vehicle identifier
     * @return marker, or {@code null} when the identifier has no valid marker
     */
    private static String extractMarker(String vehicleIdentifier) {
        var normalized = vehicleIdentifier.strip();

        if (normalized.length() < MARKER_LENGTH) {
            return null;
        }

        var marker = normalized.substring(
                normalized.length() - MARKER_LENGTH);

        return marker.chars()
                .allMatch(character -> character >= '0' && character <= '9'
                        || character >= 'A' && character <= 'Z'
                        || character >= 'a' && character <= 'z')
                ? marker
                : null;
    }

    /**
     * Reports markers associated with multiple workbook vehicles.
     *
     * @param identifiersByMarker workbook vehicles indexed by marker
     * @param problems destination for detected problems
     */
    private static void reportAmbiguousMarkers(
            Map<String, List<SheetVehicleIdentifier>> identifiersByMarker,
            List<WorkspaceAnalysisProblem> problems) {
        identifiersByMarker.forEach((marker, identifiers) -> {
            if (identifiers.size() < 2) {
                return;
            }

            var sheets = identifiers.stream()
                    .map(SheetVehicleIdentifier::sheetName)
                    .collect(Collectors.joining(", "));

            problems.add(problem(
                    AMBIGUOUS_VEHICLE_MARKER,
                    "Vehicle marker '%s' is shared by workbook sheets: %s."
                            .formatted(marker, sheets)));
        });
    }

    /**
     * Indexes the document groups in which each vehicle marker appears.
     *
     * @param documents vehicle-specific PDF documents
     * @return sorted groups indexed by marker
     */
    private static Map<String, List<Integer>> indexGroupsByMarker(
            List<VehiclePdfDocument> documents) {
        Map<String, TreeSet<Integer>> mutableGroups = new TreeMap<>();

        documents.forEach(document -> mutableGroups
                .computeIfAbsent(document.marker(), _ -> new TreeSet<>())
                .add(document.group()));

        Map<String, List<Integer>> result = new TreeMap<>();
        mutableGroups.forEach(
                (marker, groups) -> result.put(marker, List.copyOf(groups)));
        return Collections.unmodifiableMap(new TreeMap<>(result));
    }

    /**
     * Reports markers represented in more than one document group.
     *
     * @param groupsByMarker document groups indexed by vehicle marker
     * @param problems destination for detected problems
     */
    private static void reportMarkersInMultipleGroups(
            Map<String, List<Integer>> groupsByMarker,
            List<WorkspaceAnalysisProblem> problems) {
        groupsByMarker.forEach((marker, groups) -> {
            if (groups.size() < 2) {
                return;
            }

            problems.add(problem(
                    VEHICLE_IN_MULTIPLE_GROUPS,
                    "Vehicle marker '%s' appears in document groups %s."
                            .formatted(marker, groups)));
        });
    }

    /**
     * Builds every document group represented by source PDF documents.
     *
     * @param documents parsed source documents
     * @param identifiersByMarker workbook vehicles indexed by marker
     * @param identifierOrder workbook vehicle order
     * @param problems destination for detected problems
     * @return analyzed groups in numeric order
     */
    private static List<WorkspaceGroup> analyzeGroups(
            List<PdfDocument> documents,
            Map<String, List<SheetVehicleIdentifier>> identifiersByMarker,
            List<SheetVehicleIdentifier> identifierOrder,
            List<WorkspaceAnalysisProblem> problems) {
        var groupNumbers = documents.stream()
                .map(PdfDocument::group)
                .collect(Collectors.toCollection(TreeSet::new));

        var markerOrder = createMarkerOrder(identifierOrder);
        List<WorkspaceGroup> result = new ArrayList<>(groupNumbers.size());

        for (var group : groupNumbers) {
            var brochures = documents.stream()
                    .filter(GroupPdfDocument.class::isInstance)
                    .map(GroupPdfDocument.class::cast)
                    .filter(document -> document.group() == group)
                    .sorted(BY_PATH)
                    .toList();

            var groupVehicleDocuments = documents.stream()
                    .filter(VehiclePdfDocument.class::isInstance)
                    .map(VehiclePdfDocument.class::cast)
                    .filter(document -> document.group() == group)
                    .toList();

            reportGroupProblems(
                    group,
                    brochures,
                    groupVehicleDocuments,
                    problems);

            var vehicles = analyzeVehicles(
                    group,
                    groupVehicleDocuments,
                    identifiersByMarker,
                    markerOrder,
                    problems);

            result.add(new WorkspaceGroup(group, brochures, vehicles));
        }

        return List.copyOf(result);
    }

    /**
     * Creates marker positions from the workbook vehicle order.
     *
     * @param identifiers workbook vehicle inventory
     * @return first workbook position indexed by valid marker
     */
    private static Map<String, Integer> createMarkerOrder(
            List<SheetVehicleIdentifier> identifiers) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (var index = 0; index < identifiers.size(); index++) {
            var marker = extractMarker(
                    identifiers.get(index).vehicleIdentifier());

            if (marker != null) {
                result.putIfAbsent(marker, index);
            }
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(result));
    }

    /**
     * Reports missing, duplicate, or empty group-level source data.
     *
     * @param group document group
     * @param brochures group brochure documents
     * @param vehicleDocuments group vehicle documents
     * @param problems destination for detected problems
     */
    private static void reportGroupProblems(
            int group,
            List<GroupPdfDocument> brochures,
            List<VehiclePdfDocument> vehicleDocuments,
            List<WorkspaceAnalysisProblem> problems) {
        if (brochures.isEmpty()) {
            problems.add(problem(
                    MISSING_GROUP_BROCHURE,
                    "Document group %d does not contain a brochure."
                            .formatted(group)));
        } else if (brochures.size() > 1) {
            problems.add(problem(
                    DUPLICATE_GROUP_BROCHURE,
                    "Document group %d contains %d brochures."
                            .formatted(group, brochures.size())));
        }

        if (vehicleDocuments.isEmpty()) {
            problems.add(problem(
                    GROUP_WITHOUT_VEHICLES,
                    "Document group %d does not contain vehicle documents."
                            .formatted(group)));
        }
    }

    /**
     * Builds the vehicle hierarchy for one document group.
     *
     * @param group document group
     * @param documents vehicle-specific documents in the group
     * @param identifiersByMarker workbook vehicles indexed by marker
     * @param markerOrder workbook position indexed by marker
     * @param problems destination for detected problems
     * @return analyzed group vehicles
     */
    private static List<WorkspaceVehicle> analyzeVehicles(
            int group,
            List<VehiclePdfDocument> documents,
            Map<String, List<SheetVehicleIdentifier>> identifiersByMarker,
            Map<String, Integer> markerOrder,
            List<WorkspaceAnalysisProblem> problems) {
        var documentsByMarker = documents.stream()
                .collect(Collectors.groupingBy(
                        VehiclePdfDocument::marker,
                        TreeMap::new,
                        Collectors.toList()));

        var markers = documentsByMarker.keySet()
                .stream()
                .sorted(Comparator
                        .comparingInt((String marker) -> markerOrder.getOrDefault(
                                marker,
                                Integer.MAX_VALUE))
                        .thenComparing(Function.identity()))
                .toList();

        List<WorkspaceVehicle> result = new ArrayList<>(markers.size());

        for (var marker : markers) {
            var vehicleDocuments = documentsByMarker.get(marker)
                    .stream()
                    .sorted(Comparator
                            .comparingInt(
                                    VehiclePdfDocument::documentNumber)
                            .thenComparing(BY_PATH))
                    .toList();

            var identifiers = identifiersByMarker.getOrDefault(
                    marker,
                    List.of());

            if (identifiers.isEmpty()) {
                problems.add(problem(
                        UNKNOWN_VEHICLE_MARKER,
                        ("Document group %d contains marker '%s', which is not "
                                + "present in the workbook.")
                                .formatted(group, marker)));
            }

            reportDuplicateVehicleDocuments(
                    group,
                    marker,
                    vehicleDocuments,
                    problems);

            result.add(new WorkspaceVehicle(
                    marker,
                    identifiers,
                    vehicleDocuments));
        }

        return List.copyOf(result);
    }

    /**
     * Reports repeated vehicle document numbers within one group.
     *
     * @param group document group
     * @param marker vehicle marker
     * @param documents vehicle documents
     * @param problems destination for detected problems
     */
    private static void reportDuplicateVehicleDocuments(
            int group,
            String marker,
            List<VehiclePdfDocument> documents,
            List<WorkspaceAnalysisProblem> problems) {
        var counts = documents.stream()
                .collect(Collectors.groupingBy(
                        VehiclePdfDocument::documentNumber,
                        TreeMap::new,
                        Collectors.counting()));

        counts.forEach((documentNumber, count) -> {
            if (count < 2) {
                return;
            }

            problems.add(problem(
                    DUPLICATE_VEHICLE_DOCUMENT,
                    ("Document group %d contains %d documents numbered %d for "
                            + "vehicle marker '%s'.")
                            .formatted(
                                    group,
                                    count,
                                    documentNumber,
                                    marker)));
        });
    }

    /**
     * Reports workbook vehicles not represented by any PDF document.
     *
     * @param identifiers workbook vehicle inventory
     * @param groupsByMarker PDF document groups indexed by marker
     * @param ungroupedVehicles destination for ungrouped workbook vehicles
     * @param problems destination for detected problems
     */
    private static void reportVehiclesWithoutDocuments(
            List<SheetVehicleIdentifier> identifiers,
            Map<String, List<Integer>> groupsByMarker,
            List<SheetVehicleIdentifier> ungroupedVehicles,
            List<WorkspaceAnalysisProblem> problems) {
        for (var identifier : identifiers) {
            var marker = extractMarker(identifier.vehicleIdentifier());

            if (marker == null || groupsByMarker.containsKey(marker)) {
                continue;
            }

            ungroupedVehicles.add(identifier);
            problems.add(problem(
                    VEHICLE_WITHOUT_DOCUMENTS,
                    ("Vehicle identifier '%s' from sheet '%s' has no PDF "
                            + "documents.")
                            .formatted(
                                    identifier.vehicleIdentifier(),
                                    identifier.sheetName())));
        }
    }

    /**
     * Creates a problem value.
     *
     * @param type problem category
     * @param message developer-facing problem description
     * @return problem value
     */
    private static WorkspaceAnalysisProblem problem(
            WorkspaceAnalysisProblemType type,
            String message) {
        return new WorkspaceAnalysisProblem(type, message);
    }
}
