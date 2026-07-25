package com.infoyupay.mtcexpediente41.pdf;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Parses document metadata encoded in deterministic source PDF file names.
 * <br/>
 * Names begin with a group and document number separated by a dot. Document
 * number {@code 1} identifies a group brochure and therefore has no vehicle
 * marker. Every other document number must end with a four-digit marker before
 * the PDF suffix.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class PdfDocumentParser {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "^(?<group>[1-9]\\d*)\\.(?<document>[1-9]\\d*)"
                    + "(?:\\s+.*)?\\.pdf$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern MARKER_PATTERN = Pattern.compile(
            "(?:^|\\s)(?<marker>\\d{4})\\.pdf$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Parses one source PDF path.
     *
     * @param path source PDF path
     * @return parsed PDF document
     * @throws PdfDocumentNameException if the file name does not follow the
     *                                  expected convention
     */
    public @NotNull PdfDocument parse(@NotNull Path path)
            throws PdfDocumentNameException {
        Objects.requireNonNull(path, "path");

        var fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            throw new PdfDocumentNameException(
                    "PDF path does not contain a file name: " + path);
        }

        var fileName = fileNamePath.toString();
        var fileNameMatcher = FILE_NAME_PATTERN.matcher(fileName);

        if (!fileNameMatcher.matches()) {
            throw new PdfDocumentNameException(
                    "Invalid PDF document file name: " + fileName);
        }

        try {
            var group = Integer.parseInt(fileNameMatcher.group("group"));
            var documentNumber = Integer.parseInt(
                    fileNameMatcher.group("document"));

            if (documentNumber == 1) {
                return new GroupPdfDocument(group, path);
            }

            var markerMatcher = MARKER_PATTERN.matcher(fileName);
            if (!markerMatcher.find()) {
                throw new PdfDocumentNameException(
                        "Vehicle PDF document does not end with a four-digit "
                                + "marker: " + fileName);
            }

            return new VehiclePdfDocument(
                    group,
                    documentNumber,
                    markerMatcher.group("marker"),
                    path);
        } catch (NumberFormatException e) {
            throw new PdfDocumentNameException(
                    "PDF document number exceeds the supported integer range: "
                            + fileName,
                    e);
        }
    }

    /**
     * Parses every source PDF path in an iterable.
     *
     * @param paths source PDF paths
     * @return immutable parsed document list
     * @throws PdfDocumentNameException if any file name is invalid
     */
    public @NotNull List<@NotNull PdfDocument> parseAll(
            @NotNull Iterable<@NotNull Path> paths)
            throws PdfDocumentNameException {
        Objects.requireNonNull(paths, "paths");

        List<PdfDocument> documents = new ArrayList<>();
        for (var path : paths) {
            documents.add(parse(path));
        }

        return List.copyOf(documents);
    }
}
