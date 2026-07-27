package com.infoyupay.mtcexpediente41.analysis;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Describes one consistency problem found during workspace analysis.
 * <br/>
 * The {@link #type()} provides a stable value that the presentation layer can
 * translate into user-facing Spanish text. The message remains
 * developer-facing diagnostic information.
 *
 * @param type problem category
 * @param message developer-facing problem description
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public record WorkspaceAnalysisProblem(
        @NotNull WorkspaceAnalysisProblemType type,
        @NotNull String message
) {

    /**
     * Creates a workspace analysis problem.
     *
     * @param type problem category
     * @param message developer-facing problem description
     */
    public WorkspaceAnalysisProblem {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(message, "message");

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                    "Workspace analysis problem message must not be blank.");
        }
    }
}
