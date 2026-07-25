package com.infoyupay.mtcexpediente41.pdf;

/**
 * Signals that a source PDF file name does not follow the expected document
 * naming convention.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class PdfDocumentNameException extends Exception {

    /**
     * Creates an exception with a descriptive message.
     *
     * @param message exception message
     */
    public PdfDocumentNameException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a descriptive message and root cause.
     *
     * @param message exception message
     * @param cause root cause
     */
    public PdfDocumentNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
