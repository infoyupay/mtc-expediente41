package com.infoyupay.mtcexpediente41.workspace;

/**
 * Exception raised when a workspace cannot be discovered, validated, or
 * prepared for document processing.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class WorkspaceException extends Exception {

    /**
     * Creates an exception with a descriptive message.
     *
     * @param message description of the workspace problem
     */
    public WorkspaceException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a descriptive message and underlying cause.
     *
     * @param message description of the workspace problem
     * @param cause underlying failure
     */
    public WorkspaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
