package com.infoyupay.mtcexpediente41.javafx.task;

public class UnsupportedWorkbookFormatException extends RuntimeException {
    private final String fileName;

    public UnsupportedWorkbookFormatException(String fileName) {
        super("Unsupported workbook format: " + fileName);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
