package com.infoyupay.mtcexpediente41.pdf;

/**
 * Signals that a source PDF file name does not follow the expected document
 * naming convention.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class PdfDocumentNameException extends Exception {

    private final String fileName;

    /**
     * Creates an exception for the source PDF whose file name is inconsistent.
     *
     * @param fileName name of the source PDF that caused the exception
     * @param message  exception message
     */
    public PdfDocumentNameException(String fileName, String message) {
        super(message);
        this.fileName = fileName;
    }

    /**
     * Creates an exception for the source PDF whose file name is inconsistent,
     * preserving the root cause.
     *
     * @param fileName name of the source PDF that caused the exception
     * @param message  exception message
     * @param cause    root cause
     */
    public PdfDocumentNameException(String fileName, String message, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
    }

    /**
     * Returns the name of the source PDF that caused this exception.
     *
     * @return offending source PDF file name
     */
    public String getFileName() {
        return fileName;
    }
}
