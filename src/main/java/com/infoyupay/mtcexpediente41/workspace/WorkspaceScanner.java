package com.infoyupay.mtcexpediente41.workspace;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Discovers source files in a workspace and prepares its output directories.
 * <br/>
 * The scanner walks every subdirectory beneath the selected root directory,
 * excluding the generated output tree, and requires exactly one supported
 * spreadsheet workbook.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class WorkspaceScanner {

    private static final String OUTPUT_DIRECTORY_NAME = "Expedientes";
    private static final String INDIVIDUAL_DIRECTORY_NAME = "Individuales";
    private static final String CONSOLIDATED_DIRECTORY_NAME = "Consolidados";

    /**
     * Discovers the source workbook and PDF files contained in a workspace and
     * creates the directories required for generated documents.
     *
     * @param rootDirectory root directory selected by the user
     * @return prepared workspace description
     * @throws WorkspaceException if the directory cannot be scanned, contains
     *                            an invalid number of workbooks, or cannot be
     *                            prepared for output
     */
    public @NotNull Workspace scan(@NotNull Path rootDirectory)
            throws WorkspaceException {
        Objects.requireNonNull(rootDirectory, "rootDirectory");

        var root = rootDirectory.toAbsolutePath().normalize();
        validateRootDirectory(root);

        var outputDirectory = root.resolve(OUTPUT_DIRECTORY_NAME);
        var individualOutputDirectory =
                outputDirectory.resolve(INDIVIDUAL_DIRECTORY_NAME);
        var consolidatedOutputDirectory =
                outputDirectory.resolve(CONSOLIDATED_DIRECTORY_NAME);

        var sourceFiles = discoverSourceFiles(root, outputDirectory);
        var workbooks = sourceFiles.stream()
                .filter(WorkspaceScanner::isWorkbook)
                .sorted()
                .toList();

        if (workbooks.size() != 1) {
            throw new WorkspaceException(
                    "Workspace must contain exactly one XLSX or ODS "
                            + "workbook, but found "
                            + workbooks.size()
                            + '.');
        }

        var pdfFiles = sourceFiles.stream()
                .filter(WorkspaceScanner::isPdf)
                .sorted()
                .toList();

        createOutputDirectories(
                individualOutputDirectory,
                consolidatedOutputDirectory);

        return new Workspace(
                root,
                workbooks.getFirst(),
                pdfFiles,
                outputDirectory,
                individualOutputDirectory,
                consolidatedOutputDirectory);
    }

    /**
     * Verifies that the supplied path identifies a readable directory.
     *
     * @param rootDirectory normalized root directory
     * @throws WorkspaceException if the path is missing, is not a directory,
     *                            or cannot be read
     */
    private static void validateRootDirectory(Path rootDirectory)
            throws WorkspaceException {
        if (!Files.exists(rootDirectory)) {
            throw new WorkspaceException(
                    "Workspace directory does not exist: " + rootDirectory);
        }

        if (!Files.isDirectory(rootDirectory)) {
            throw new WorkspaceException(
                    "Workspace path is not a directory: " + rootDirectory);
        }

        if (!Files.isReadable(rootDirectory)) {
            throw new WorkspaceException(
                    "Workspace directory is not readable: " + rootDirectory);
        }
    }

    /**
     * Recursively discovers regular source files while excluding the generated
     * output directory and all of its descendants.
     *
     * @param rootDirectory root directory to scan
     * @param outputDirectory generated output tree to exclude
     * @return discovered regular files
     * @throws WorkspaceException if the file tree cannot be traversed
     */
    private static List<Path> discoverSourceFiles(
            Path rootDirectory,
            Path outputDirectory) throws WorkspaceException {
        List<Path> sourceFiles = new ArrayList<>();

        try {
            Files.walkFileTree(
                    rootDirectory,
                    new SimpleFileVisitor<>() {
                        /**
                         * Excludes the generated output tree from traversal.
                         *
                         * @param directory current directory
                         * @param attributes directory attributes
                         * @return visit instruction for the current directory
                         */
                        @Override
                        public @NotNull FileVisitResult preVisitDirectory(
                                @NotNull Path directory,
                                @NotNull BasicFileAttributes attributes) {
                            if (directory.startsWith(outputDirectory)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        /**
                         * Adds each regular source file to the result.
                         *
                         * @param file current file
                         * @param attributes file attributes
                         * @return instruction to continue traversal
                         */
                        @Override
                        public @NotNull FileVisitResult visitFile(
                                @NotNull Path file,
                                @NotNull BasicFileAttributes attributes) {
                            if (attributes.isRegularFile()) {
                                sourceFiles.add(file);
                            }

                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new WorkspaceException(
                    "Unable to scan workspace directory: " + rootDirectory,
                    e);
        }

        return List.copyOf(sourceFiles);
    }

    /**
     * Creates the directories used for individual and consolidated documents.
     *
     * @param individualOutputDirectory directory for individual expedients
     * @param consolidatedOutputDirectory directory for consolidated documents
     * @throws WorkspaceException if either directory cannot be created
     */
    private static void createOutputDirectories(
            Path individualOutputDirectory,
            Path consolidatedOutputDirectory) throws WorkspaceException {
        try {
            Files.createDirectories(individualOutputDirectory);
            Files.createDirectories(consolidatedOutputDirectory);
        } catch (IOException e) {
            throw new WorkspaceException(
                    "Unable to create workspace output directories.",
                    e);
        }
    }

    /**
     * Determines whether a path identifies a supported spreadsheet workbook.
     *
     * @param path file path
     * @return {@code true} for XLSX and ODS workbooks
     */
    private static boolean isWorkbook(Path path) {
        var fileName = lowerCaseFileName(path);
        return fileName.endsWith(".xlsx") || fileName.endsWith(".ods");
    }

    /**
     * Determines whether a path identifies a PDF source document.
     *
     * @param path file path
     * @return {@code true} for PDF files
     */
    private static boolean isPdf(Path path) {
        return lowerCaseFileName(path).endsWith(".pdf");
    }

    /**
     * Obtains a locale-independent lower-case file name.
     *
     * @param path file path
     * @return normalized file name
     */
    private static String lowerCaseFileName(Path path) {
        return path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);
    }
}
