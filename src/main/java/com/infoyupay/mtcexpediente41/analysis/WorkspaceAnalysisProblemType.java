package com.infoyupay.mtcexpediente41.analysis;

/**
 * Identifies the consistency problems that may be found while correlating
 * workbook vehicles and source PDF documents.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public enum WorkspaceAnalysisProblemType {

    /**
     * A workbook vehicle identifier does not end with a four-digit marker.
     */
    INVALID_VEHICLE_MARKER,

    /**
     * More than one workbook vehicle identifier shares the same marker.
     */
    AMBIGUOUS_VEHICLE_MARKER,

    /**
     * A vehicle marker is represented by PDF documents in multiple groups.
     */
    VEHICLE_IN_MULTIPLE_GROUPS,

    /**
     * A PDF document marker has no corresponding workbook vehicle.
     */
    UNKNOWN_VEHICLE_MARKER,

    /**
     * A workbook vehicle has no corresponding PDF documents.
     */
    VEHICLE_WITHOUT_DOCUMENTS,

    /**
     * A document group does not contain its shared brochure.
     */
    MISSING_GROUP_BROCHURE,

    /**
     * A document group contains more than one shared brochure.
     */
    DUPLICATE_GROUP_BROCHURE,

    /**
     * A document group contains no vehicle-specific documents.
     */
    GROUP_WITHOUT_VEHICLES,

    /**
     * A vehicle contains more than one PDF with the same document number in
     * one group.
     */
    DUPLICATE_VEHICLE_DOCUMENT
}
