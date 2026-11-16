package com.infoyupay.mtcexpediente41.output;

import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.List;

/** Immutable description of generated individual and group expedientes. */
public record WorkspacePdfOutput(
        @NotNull List<Path> individualFiles,
        @NotNull List<Path> groupFiles
) {
    public WorkspacePdfOutput {
        individualFiles = List.copyOf(individualFiles);
        groupFiles = List.copyOf(groupFiles);
    }
}
