package com.infoyupay.mtcexpediente41.javafx.task;

/**
 * Indicates that a workspace workbook uses an unsupported file format.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public class UnsupportedWorkbookFormatException extends RuntimeException {
    private final String fileName;

    /**
     * Creates an exception for the unsupported workbook.
     *
     * @param fileName unsupported workbook file name
     */
    public UnsupportedWorkbookFormatException(String fileName) {
        super("Unsupported workbook format: " + fileName);
        this.fileName = fileName;
    }

    /**
     * Returns the unsupported workbook file name.
     *
     * @return unsupported workbook file name
     */
    public String getFileName() {
        return fileName;
    }
}
