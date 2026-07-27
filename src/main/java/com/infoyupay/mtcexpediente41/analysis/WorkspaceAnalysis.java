package com.infoyupay.mtcexpediente41.analysis;

import com.infoyupay.mtcexpediente41.datasheet.SheetVehicleIdentifier;
import com.infoyupay.mtcexpediente41.workspace.Workspace;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Provides the immutable intermediate result produced before document
 * generation.
 * <br/>
 * The result retains the complete workbook inventory, the correlated
 * group-vehicle-document hierarchy, workbook vehicles that have not been
 * assigned to any group, and every detected consistency problem.
 *
 * @param workspace analyzed physical workspace
 * @param vehicleIdentifiers complete workbook vehicle inventory
 * @param groups correlated document groups
 * @param ungroupedVehicleIdentifiers workbook vehicles without usable PDF
 *                                    documents
 * @param problems detected consistency problems
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record WorkspaceAnalysis(
        @NotNull Workspace workspace,
        @NotNull List<@NotNull SheetVehicleIdentifier> vehicleIdentifiers,
        @NotNull List<@NotNull WorkspaceGroup> groups,
        @NotNull List<@NotNull SheetVehicleIdentifier>
                ungroupedVehicleIdentifiers,
        @NotNull List<@NotNull WorkspaceAnalysisProblem> problems
) {

    /**
     * Creates an immutable workspace analysis result.
     *
     * @param workspace analyzed physical workspace
     * @param vehicleIdentifiers complete workbook vehicle inventory
     * @param groups correlated document groups
     * @param ungroupedVehicleIdentifiers workbook vehicles without usable PDF
     *                                    documents
     * @param problems detected consistency problems
     */
    public WorkspaceAnalysis {
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(vehicleIdentifiers, "vehicleIdentifiers");
        Objects.requireNonNull(groups, "groups");
        Objects.requireNonNull(
                ungroupedVehicleIdentifiers,
                "ungroupedVehicleIdentifiers");
        Objects.requireNonNull(problems, "problems");

        vehicleIdentifiers = List.copyOf(vehicleIdentifiers);
        groups = List.copyOf(groups);
        ungroupedVehicleIdentifiers =
                List.copyOf(ungroupedVehicleIdentifiers);
        problems = List.copyOf(problems);
    }

    /**
     * Returns the number of document groups represented by the source PDFs.
     *
     * @return document group count
     */
    public int groupCount() {
        return groups.size();
    }

    /**
     * Returns the number of vehicles declared by the workbook.
     *
     * @return workbook vehicle count
     */
    public int vehicleCount() {
        return vehicleIdentifiers.size();
    }

    /**
     * Determines whether the workspace is internally consistent and can
     * proceed to document generation.
     *
     * @return {@code true} when no analysis problems were found
     */
    public boolean canProceed() {
        return problems.isEmpty();
    }
}
