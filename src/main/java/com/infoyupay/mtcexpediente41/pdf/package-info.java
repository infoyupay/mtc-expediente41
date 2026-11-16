/**
 * Provides models and parsers for source PDF documents whose deterministic
 * file names identify their group, document number, and optional vehicle
 * marker.
 * <br/>
 * Group documents are shared by every vehicle in their group and therefore do
 * not carry a marker. Vehicle documents carry a four-character alphanumeric
 * marker. Both types are interleaved in numeric position order when producing
 * individual and complete group expedientes.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
package com.infoyupay.mtcexpediente41.pdf;
