package com.infoyupay.mtcexpediente41.output;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Concatenates ordered source PDF files into one destination document.
 * <br/>
 * The merger writes through a temporary sibling file and publishes the result
 * only after PDFBox completes successfully. Existing destination files are
 * never overwritten.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class PdfFileMerger {

    /**
     * Creates a stateless PDF file merger.
     */
    public PdfFileMerger() {
    }

    /**
     * Concatenates source documents in their supplied order.
     *
     * @param sources ordered source PDF paths
     * @param destination destination PDF path
     * @return normalized absolute destination path
     * @throws IOException if a source cannot be read, the destination already
     *                     exists, or the merged document cannot be written
     */
    public @NotNull Path merge(
            @NotNull List<@NotNull Path> sources,
            @NotNull Path destination) throws IOException {
        Objects.requireNonNull(sources, "sources");
        Objects.requireNonNull(destination, "destination");

        var orderedSources = sources.stream()
                .map(source -> source.toAbsolutePath().normalize())
                .toList();
        if (orderedSources.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one source PDF is required.");
        }

        var target = destination.toAbsolutePath().normalize();
        var parent = Objects.requireNonNull(
                target.getParent(),
                "Destination must have a parent directory.");

        for (var source : orderedSources) {
            if (target.equals(source)) {
                throw new IllegalArgumentException(
                        "Destination cannot also be a source PDF.");
            }
            if (!Files.isRegularFile(source)) {
                throw new NoSuchFileException(source.toString());
            }
        }

        Files.createDirectories(parent);
        if (Files.exists(target)) {
            throw new FileAlreadyExistsException(target.toString());
        }

        var temporaryFile = Files.createTempFile(
                parent,
                ".expediente41-",
                ".pdf.tmp");

        try {
            var merger = new PDFMergerUtility();
            for (var source : orderedSources) {
                merger.addSource(source.toFile());
            }
            merger.setDestinationFileName(temporaryFile.toString());
            merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());

            return Files.move(temporaryFile, target);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}
