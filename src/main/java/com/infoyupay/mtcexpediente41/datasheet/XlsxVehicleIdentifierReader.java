package com.infoyupay.mtcexpediente41.datasheet;

import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the canonical vehicle identifier from cell C13 of every worksheet in
 * an XLSX workbook.
 * <br/>
 * This reader uses only ZIP and StAX facilities supplied by the JDK. Formulae
 * are not evaluated; when the target cell contains a formula, its cached value
 * stored in the workbook is returned.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class XlsxVehicleIdentifierReader {

    private static final String TARGET_CELL = "C13";

    private static final String WORKBOOK_ENTRY = "xl/workbook.xml";
    private static final String WORKBOOK_RELATIONSHIPS_ENTRY =
            "xl/_rels/workbook.xml.rels";
    private static final String SHARED_STRINGS_ENTRY =
            "xl/sharedStrings.xml";

    private static final String RELATIONSHIPS_NAMESPACE =
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    /**
     * Reads the vehicle identifier contained in cell C13 of every worksheet.
     *
     * @param workbook path to the XLSX workbook
     * @return worksheet names and their vehicle identifiers, preserving
     * workbook order
     * @throws IOException if the workbook cannot be opened or parsed
     */
    public @NotNull List<SheetVehicleIdentifier> read(
            @NotNull Path workbook
    ) throws IOException {
        Objects.requireNonNull(workbook, "workbook");

        try (ZipFile zipFile = new ZipFile(workbook.toFile())) {
            List<String> sharedStrings = readSharedStrings(zipFile);
            Map<String, String> relationships =
                    readWorkbookRelationships(zipFile);
            List<WorksheetReference> worksheets =
                    readWorksheetReferences(zipFile);

            List<SheetVehicleIdentifier> result =
                    new ArrayList<>(worksheets.size());

            for (WorksheetReference worksheet : worksheets) {
                String relationshipTarget =
                        relationships.get(worksheet.relationshipId());

                if (relationshipTarget == null) {
                    throw new IOException(
                            "No workbook relationship found for worksheet '%s'."
                                    .formatted(worksheet.name()));
                }

                String worksheetEntry =
                        resolveWorksheetEntry(relationshipTarget);

                String identifier = readCellValue(
                        zipFile,
                        worksheetEntry,
                        TARGET_CELL,
                        sharedStrings);

                result.add(new SheetVehicleIdentifier(
                        worksheet.name(),
                        identifier));
            }

            return List.copyOf(result);
        }
    }

    /**
     * Reads worksheet names and relationship identifiers from workbook.xml.
     *
     * @param zipFile open XLSX package
     * @return worksheet references in workbook order
     * @throws IOException if workbook.xml is absent or malformed
     */
    private List<WorksheetReference> readWorksheetReferences(
            ZipFile zipFile
    ) throws IOException {
        ZipEntry entry = requireEntry(zipFile, WORKBOOK_ENTRY);
        List<WorksheetReference> result = new ArrayList<>();

        try (InputStream input = zipFile.getInputStream(entry)) {
            XMLStreamReader reader = newXmlReader(input);

            try {
                while (reader.hasNext()) {
                    int event = reader.next();

                    if (event != XMLStreamConstants.START_ELEMENT
                            || !"sheet".equals(reader.getLocalName())) {
                        continue;
                    }

                    String name = reader.getAttributeValue(null, "name");
                    String relationshipId = reader.getAttributeValue(
                            RELATIONSHIPS_NAMESPACE,
                            "id");

                    if (name == null || relationshipId == null) {
                        throw new IOException(
                                "Malformed worksheet declaration in "
                                        + WORKBOOK_ENTRY);
                    }

                    result.add(new WorksheetReference(
                            name,
                            relationshipId));
                }
            } catch (XMLStreamException e) {
                throw new IOException(
                        "Cannot parse " + WORKBOOK_ENTRY,
                        e);
            } finally {
                closeReader(reader);
            }
        }

        return result;
    }

    /**
     * Reads workbook relationship targets indexed by relationship ID.
     *
     * @param zipFile open XLSX package
     * @return relationship targets by identifier
     * @throws IOException if the relationships document is absent or malformed
     */
    private Map<String, String> readWorkbookRelationships(
            ZipFile zipFile
    ) throws IOException {
        ZipEntry entry = requireEntry(
                zipFile,
                WORKBOOK_RELATIONSHIPS_ENTRY);

        Map<String, String> result = new HashMap<>();

        try (InputStream input = zipFile.getInputStream(entry)) {
            XMLStreamReader reader = newXmlReader(input);

            try {
                while (reader.hasNext()) {
                    int event = reader.next();

                    if (event != XMLStreamConstants.START_ELEMENT
                            || !"Relationship".equals(
                            reader.getLocalName())) {
                        continue;
                    }

                    String id = reader.getAttributeValue(null, "Id");
                    String target =
                            reader.getAttributeValue(null, "Target");

                    if (id != null && target != null) {
                        result.put(id, target);
                    }
                }
            } catch (XMLStreamException e) {
                throw new IOException(
                        "Cannot parse "
                                + WORKBOOK_RELATIONSHIPS_ENTRY,
                        e);
            } finally {
                closeReader(reader);
            }
        }

        return Map.copyOf(result);
    }

    /**
     * Reads the workbook shared-string table when present.
     *
     * @param zipFile open XLSX package
     * @return shared strings in index order
     * @throws IOException if the shared-string table is malformed
     */
    private List<String> readSharedStrings(
            ZipFile zipFile
    ) throws IOException {
        ZipEntry entry = zipFile.getEntry(SHARED_STRINGS_ENTRY);

        if (entry == null) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        try (InputStream input = zipFile.getInputStream(entry)) {
            XMLStreamReader reader = newXmlReader(input);

            try {
                StringBuilder currentString = null;

                while (reader.hasNext()) {
                    int event = reader.next();

                    if (event == XMLStreamConstants.START_ELEMENT
                            && "si".equals(reader.getLocalName())) {
                        currentString = new StringBuilder();
                    } else if (event == XMLStreamConstants.START_ELEMENT
                            && "t".equals(reader.getLocalName())
                            && currentString != null) {
                        currentString.append(
                                reader.getElementText());
                    } else if (event == XMLStreamConstants.END_ELEMENT
                            && "si".equals(reader.getLocalName())
                            && currentString != null) {
                        result.add(currentString.toString());
                        currentString = null;
                    }
                }
            } catch (XMLStreamException e) {
                throw new IOException(
                        "Cannot parse " + SHARED_STRINGS_ENTRY,
                        e);
            } finally {
                closeReader(reader);
            }
        }

        return List.copyOf(result);
    }

    /**
     * Reads one cell value from a worksheet.
     *
     * @param zipFile open XLSX package
     * @param worksheetEntry worksheet ZIP entry
     * @param cellReference requested cell reference
     * @param sharedStrings workbook shared strings
     * @return stored cell value, or an empty string if the cell is absent
     * @throws IOException if the worksheet cannot be parsed
     */
    private String readCellValue(
            ZipFile zipFile,
            String worksheetEntry,
            @SuppressWarnings("SameParameterValue") String cellReference,
            List<String> sharedStrings
    ) throws IOException {
        ZipEntry entry = requireEntry(zipFile, worksheetEntry);

        try (InputStream input = zipFile.getInputStream(entry)) {
            XMLStreamReader reader = newXmlReader(input);

            try {
                while (reader.hasNext()) {
                    int event = reader.next();

                    if (event != XMLStreamConstants.START_ELEMENT
                            || !"c".equals(reader.getLocalName())) {
                        continue;
                    }

                    String reference =
                            reader.getAttributeValue(null, "r");

                    if (!cellReference.equals(reference)) {
                        continue;
                    }

                    String type =
                            reader.getAttributeValue(null, "t");

                    return readCurrentCellValue(
                            reader,
                            type,
                            sharedStrings);
                }

                return "";
            } catch (XMLStreamException e) {
                throw new IOException(
                        "Cannot parse worksheet " + worksheetEntry,
                        e);
            } finally {
                closeReader(reader);
            }
        }
    }

    /**
     * Reads the content of the cell on which the reader is currently
     * positioned.
     *
     * @param reader worksheet XML reader
     * @param cellType XLSX cell type
     * @param sharedStrings workbook shared strings
     * @return decoded cell value
     * @throws XMLStreamException if the XML stream cannot be read
     * @throws IOException if the cell contains an invalid shared-string index
     */
    private String readCurrentCellValue(
            XMLStreamReader reader,
            String cellType,
            List<String> sharedStrings
    ) throws XMLStreamException, IOException {
        String rawValue = "";
        StringBuilder inlineValue = new StringBuilder();

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("v".equals(reader.getLocalName())) {
                    rawValue = reader.getElementText();
                } else if ("t".equals(reader.getLocalName())) {
                    inlineValue.append(reader.getElementText());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "c".equals(reader.getLocalName())) {
                break;
            }
        }

        if ("s".equals(cellType)) {
            return resolveSharedString(rawValue, sharedStrings);
        }

        if ("inlineStr".equals(cellType)) {
            return inlineValue.toString();
        }

        return rawValue;
    }

    /**
     * Resolves a value from the shared-string table.
     *
     * @param rawIndex textual shared-string index
     * @param sharedStrings workbook shared strings
     * @return resolved string
     * @throws IOException if the index is absent or invalid
     */
    private String resolveSharedString(
            String rawIndex,
            List<String> sharedStrings
    ) throws IOException {
        try {
            int index = Integer.parseInt(rawIndex);

            if (index < 0 || index >= sharedStrings.size()) {
                throw new IOException(
                        "Shared-string index out of bounds: " + index);
            }

            return sharedStrings.get(index);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "Invalid shared-string index: " + rawIndex,
                    e);
        }
    }

    /**
     * Resolves a worksheet relationship target to its XLSX package entry.
     *
     * @param target relationship target
     * @return normalized ZIP entry path
     */
    private String resolveWorksheetEntry(String target) {
        String normalized = target.replace('\\', '/');

        if (normalized.startsWith("/")) {
            return normalized.substring(1);
        }

        if (normalized.startsWith("xl/")) {
            return normalized;
        }

        while (normalized.startsWith("../")) {
            normalized = normalized.substring(3);
        }

        return "xl/" + normalized;
    }

    /**
     * Obtains a required entry from the XLSX ZIP package.
     *
     * @param zipFile open XLSX package
     * @param entryName required entry name
     * @return matching ZIP entry
     * @throws IOException if the entry does not exist
     */
    private ZipEntry requireEntry(
            ZipFile zipFile,
            String entryName
    ) throws IOException {
        ZipEntry entry = zipFile.getEntry(entryName);

        if (entry == null) {
            throw new IOException(
                    "Required XLSX entry not found: " + entryName);
        }

        return entry;
    }

    /**
     * Creates a hardened StAX reader.
     *
     * @param input XML input stream
     * @return configured XML stream reader
     * @throws IOException if the reader cannot be created
     */
    private XMLStreamReader newXmlReader(
            InputStream input
    ) throws IOException {
        XMLInputFactory factory = XMLInputFactory.newFactory();

        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(
                "javax.xml.stream.isSupportingExternalEntities",
                false);

        try {
            return factory.createXMLStreamReader(input);
        } catch (XMLStreamException e) {
            throw new IOException("Cannot create XML stream reader.", e);
        }
    }

    /**
     * Closes an XML reader without masking the original parsing exception.
     *
     * @param reader reader to close
     */
    private void closeReader(XMLStreamReader reader) {
        try {
            reader.close();
        } catch (XMLStreamException _) {
            // InputStream closure remains authoritative.
        }
    }

    /**
     * Identifies a worksheet through its name and workbook relationship.
     *
     * @param name worksheet display name
     * @param relationshipId workbook relationship identifier
     */
    private record WorksheetReference(
            String name,
            String relationshipId
    ) {
    }
}