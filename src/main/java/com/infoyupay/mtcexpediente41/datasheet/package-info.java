/**
 * Provides components for reading vehicle identifiers from spreadsheet
 * datasheets used during document package preparation.
 * <br/>
 * Supported workbook formats expose one worksheet per vehicle, where each
 * worksheet contributes the vehicle identifier used throughout the document
 * assembly pipeline. Format-specific readers provide a common abstraction for
 * accessing these identifiers independently of the underlying spreadsheet
 * format.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
package com.infoyupay.mtcexpediente41.datasheet;