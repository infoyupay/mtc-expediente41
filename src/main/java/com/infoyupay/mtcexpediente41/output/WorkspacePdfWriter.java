package com.infoyupay.mtcexpediente41.output;

import com.infoyupay.mtcexpediente41.analysis.WorkspaceAnalysis;
import com.infoyupay.mtcexpediente41.analysis.WorkspaceGroup;
import com.infoyupay.mtcexpediente41.analysis.WorkspaceVehicle;
import com.infoyupay.mtcexpediente41.pdf.PdfDocument;
import com.infoyupay.mtcexpediente41.pdf.VehiclePdfDocument;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Generates individual and per-group PDF documents from a workspace analysis.
 * <br/>
 * Every output produced by one invocation shares a version timestamp. An
 * individual document includes shared and vehicle PDFs in numeric position
 * order. Each group output includes its shared PDFs once and all vehicle PDFs,
 * ordered by document number and marker.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class WorkspacePdfWriter {

    /**
     * Document number assigned to vehicle requests.
     */
    private static final int REQUEST_DOCUMENT_NUMBER = 2;

    /**
     * Document number assigned to vehicle technical sheets.
     */
    private static final int TECHNICAL_SHEET_DOCUMENT_NUMBER = 3;

    /**
     * Formats the version suffix used by every generated file.
     */
    private static final DateTimeFormatter VERSION_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "'ver'yyyy-MM-dd_HH-mm",
                    Locale.ROOT);

    private final Clock clock;
    private final PdfFileMerger merger;

    /**
     * Creates a writer that uses the system clock and default time zone.
     */
    public WorkspacePdfWriter() {
        this(Clock.systemDefaultZone(), new PdfFileMerger());
    }

    /**
     * Creates a writer with a caller-supplied clock.
     * <br/>
     * This constructor has package visibility so timestamp behavior can be
     * tested deterministically without expanding the public API.
     *
     * @param clock clock used to timestamp generated files
     */
    WorkspacePdfWriter(@NotNull Clock clock) {
        this(clock, new PdfFileMerger());
    }

    /**
     * Creates a writer with all collaborators supplied.
     *
     * @param clock clock used to timestamp generated files
     * @param merger low-level PDF merger
     */
    private WorkspacePdfWriter(
            @NotNull Clock clock,
            @NotNull PdfFileMerger merger) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.merger = Objects.requireNonNull(merger, "merger");
    }

    /**
     * Writes every PDF required by a consistent workspace analysis.
     *
     * @param analysis consistent workspace analysis
     * @return immutable description of the generated files
     * @throws IOException if an output file already exists or any document
     *                     cannot be merged
     */
    public @NotNull WorkspacePdfOutput write(
            @NotNull WorkspaceAnalysis analysis) throws IOException {
        Objects.requireNonNull(analysis, "analysis");
        if (!analysis.canProceed()) {
            throw new IllegalArgumentException(
                    "Workspace analysis contains blocking problems.");
        }

        var version = VERSION_FORMATTER.format(LocalDateTime.now(clock));
        Map<Path, List<Path>> mergePlan = new LinkedHashMap<>();
        List<Path> individualFiles = new ArrayList<>();
        List<Path> groupFiles = new ArrayList<>();

        for (var group : analysis.groups()) {
            if (group.commonDocuments().isEmpty()) {
                throw new IllegalArgumentException(
                        "Document group must contain at least one shared document.");
            }
            List<PdfDocument> groupDocuments = new ArrayList<>(group.commonDocuments());
            for (var vehicle : group.vehicles()) {
                requireMandatoryDocuments(group, vehicle);
                var identifier = vehicle.identifier().orElseThrow(() ->
                        new IllegalArgumentException("Vehicle marker must identify exactly one workbook vehicle."));
                var destination = analysis.workspace().individualOutputDirectory()
                        .resolve(individualFileName(identifier.vehicleIdentifier(), version));
                addMerge(mergePlan, destination, group.documentsFor(vehicle).stream()
                        .map(PdfDocument::path).toList());
                individualFiles.add(destination.toAbsolutePath().normalize());
                groupDocuments.addAll(vehicle.documents());
            }
            groupDocuments.sort(Comparator.comparingInt(PdfDocument::documentNumber)
                    .thenComparing(document -> document instanceof VehiclePdfDocument vehicle
                            ? vehicle.marker() : "")
                    .thenComparing(document -> document.path().toString()));
            var destination = analysis.workspace().consolidatedOutputDirectory()
                    .resolve("Expediente Grupo %d %s.pdf".formatted(group.group(), version));
            addMerge(mergePlan, destination, groupDocuments.stream().map(PdfDocument::path).toList());
            groupFiles.add(destination.toAbsolutePath().normalize());
        }

        ensureDestinationsAvailable(mergePlan.keySet());
        executeMergePlan(mergePlan);

        return new WorkspacePdfOutput(individualFiles, groupFiles);
    }

    /**
     * Ensures that one analyzed vehicle has its mandatory request and technical
     * sheet documents.
     *
     * @param group analyzed document group
     * @param vehicle analyzed vehicle
     */
    private static void requireMandatoryDocuments(
            WorkspaceGroup group,
            WorkspaceVehicle vehicle) {
        var hasRequest = group.documentsFor(vehicle)
                .stream()
                .anyMatch(document ->
                        document.documentNumber() == REQUEST_DOCUMENT_NUMBER);
        var hasTechnicalSheet = group.documentsFor(vehicle)
                .stream()
                .anyMatch(document -> document.documentNumber()
                        == TECHNICAL_SHEET_DOCUMENT_NUMBER);

        if (!hasRequest || !hasTechnicalSheet) {
            throw new IllegalArgumentException(
                    ("Vehicle marker '%s' in document group %d must contain "
                            + "documents 2 and 3.")
                            .formatted(vehicle.marker(), group.group()));
        }
    }

    /**
     * Creates the individual output file name for one vehicle.
     *
     * @param vehicleIdentifier complete vehicle identifier
     * @param version formatted version timestamp
     * @return individual PDF file name
     */
    private static String individualFileName(
            String vehicleIdentifier,
            String version) {
        var identifier = Objects.requireNonNull(
                vehicleIdentifier,
                "vehicleIdentifier").strip();
        var forbiddenCharacters = "\\/:*?\"<>|";

        if (identifier.isEmpty()
                || identifier.chars().anyMatch(character ->
                Character.isISOControl(character)
                        || forbiddenCharacters.indexOf(character) >= 0)) {
            throw new IllegalArgumentException(
                    "Vehicle identifier cannot be used as a file name.");
        }

        return "%s %s.pdf".formatted(identifier, version);
    }

    /**
     * Adds one destination and its ordered sources to a merge plan.
     *
     * @param mergePlan mutable merge plan
     * @param destination destination PDF path
     * @param sources ordered source PDF paths
     */
    private static void addMerge(
            Map<Path, List<Path>> mergePlan,
            Path destination,
            List<Path> sources) {
        var normalizedDestination =
                destination.toAbsolutePath().normalize();
        var previous = mergePlan.putIfAbsent(
                normalizedDestination,
                List.copyOf(sources));

        if (previous != null) {
            throw new IllegalArgumentException(
                    "Multiple outputs resolve to the same destination: "
                            + normalizedDestination);
        }
    }

    /**
     * Verifies that no planned destination already exists.
     *
     * @param destinations planned destination paths
     * @throws FileAlreadyExistsException if a destination already exists
     */
    private static void ensureDestinationsAvailable(
            Collection<Path> destinations)
            throws FileAlreadyExistsException {
        for (var destination : destinations) {
            if (Files.exists(destination)) {
                throw new FileAlreadyExistsException(
                        destination.toString());
            }
        }
    }

    /**
     * Executes a merge plan and removes files created by the current invocation
     * if a later merge fails.
     *
     * @param mergePlan destinations associated with ordered source paths
     * @throws IOException if a source cannot be read or an output cannot be
     *                     written
     */
    private void executeMergePlan(
            Map<Path, List<Path>> mergePlan) throws IOException {
        List<Path> createdFiles = new ArrayList<>();

        try {
            for (var entry : mergePlan.entrySet()) {
                createdFiles.add(merger.merge(
                        entry.getValue(),
                        entry.getKey()));
            }
        } catch (IOException | RuntimeException exception) {
            deleteGeneratedFiles(createdFiles, exception);
            throw exception;
        }
    }

    /**
     * Deletes outputs created before a failed merge.
     *
     * @param files generated output paths
     * @param failure failure that triggered cleanup
     */
    private static void deleteGeneratedFiles(
            List<Path> files,
            Throwable failure) {
        for (var index = files.size() - 1; index >= 0; index--) {
            try {
                Files.deleteIfExists(files.get(index));
            } catch (IOException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
        }
    }
}
