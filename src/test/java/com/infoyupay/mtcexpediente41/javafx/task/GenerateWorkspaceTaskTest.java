package com.infoyupay.mtcexpediente41.javafx.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link GenerateWorkspaceTask}.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class GenerateWorkspaceTaskTest {

    /**
     * Verifies that a generation task cannot be created without a completed
     * workspace analysis.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void should_reject_missing_workspace_analysis() {
        assertThatNullPointerException()
                .isThrownBy(() -> new GenerateWorkspaceTask(null))
                .withMessage("analysis");
    }
}
