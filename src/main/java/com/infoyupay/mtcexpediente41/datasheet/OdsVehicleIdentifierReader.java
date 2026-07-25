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
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

/**
 * Reads vehicle identifiers from OpenDocument spreadsheet workbooks.
 * <br/>
 * Each worksheet contributes the cached value stored in cell C13.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class OdsVehicleIdentifierReader implements VehicleIdentifierReader {

    private static final String CONTENT_XML = "content.xml";

    private static final String TABLE_NAMESPACE =
            "urn:oasis:names:tc:opendocument:xmlns:table:1.0";

    private static final String OFFICE_NAMESPACE =
            "urn:oasis:names:tc:opendocument:xmlns:office:1.0";

    private static final String TEXT_NAMESPACE =
            "urn:oasis:names:tc:opendocument:xmlns:text:1.0";

    private static final int TARGET_ROW = 13;
    private static final int TARGET_COLUMN = 3;

    private final XMLInputFactory xmlInputFactory;

    /**
     * Creates an ODS reader using a default StAX input factory.
     */
    public OdsVehicleIdentifierReader() {
        xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(
                XMLInputFactory.SUPPORT_DTD,
                false);
        xmlInputFactory.setProperty(
                "javax.xml.stream.isSupportingExternalEntities",
                false);
    }

    /**
     * Reads worksheet names and their corresponding vehicle identifiers.
     *
     * @param workbook path to the ODS workbook
     * @return vehicle identifiers indexed by worksheet
     * @throws IOException if the workbook cannot be opened or read
     */
    public @NotNull List<SheetVehicleIdentifier> read(
            @NotNull Path workbook) throws IOException {
        Objects.requireNonNull(workbook, "workbook");

        try (var zipFile = new ZipFile(workbook.toFile())) {
            var contentEntry = zipFile.getEntry(CONTENT_XML);

            if (contentEntry == null) {
                throw new IOException(
                        "ODS workbook does not contain " + CONTENT_XML);
            }

            try (var input = zipFile.getInputStream(contentEntry)) {
                return readContent(input);
            }
        }
    }

    /**
     * Reads spreadsheet data from an ODS content stream.
     *
     * @param input content.xml input stream
     * @return worksheet identifiers
     * @throws IOException if the XML content cannot be parsed
     */
    private List<SheetVehicleIdentifier> readContent(InputStream input)
            throws IOException {
        try {
            var reader =
                    xmlInputFactory.createXMLStreamReader(input);

            try {
                return parseDocument(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new IOException("Unable to parse ODS content", e);
        }
    }

    /**
     * Parses all spreadsheet tables contained in the document.
     *
     * @param reader XML stream reader
     * @return parsed worksheet identifiers
     * @throws XMLStreamException if malformed XML is encountered
     * @throws IOException        if a target cell cannot be read
     */
    private List<SheetVehicleIdentifier> parseDocument(
            XMLStreamReader reader)
            throws XMLStreamException, IOException {
        List<SheetVehicleIdentifier> result = new ArrayList<>();

        while (reader.hasNext()) {
            var event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT
                    && isTableElement(reader, "table")) {
                var sheetName = reader.getAttributeValue(
                        TABLE_NAMESPACE,
                        "name");

                var identifier = readTargetCell(reader, sheetName);

                result.add(new SheetVehicleIdentifier(
                        sheetName,
                        identifier));
            }
        }

        return List.copyOf(result);
    }

    /**
     * Reads the logical C13 cell from the current worksheet.
     *
     * @param reader    XML reader positioned at a table element
     * @param sheetName worksheet name
     * @return cached cell value
     * @throws XMLStreamException if XML parsing fails
     * @throws IOException        if C13 is missing
     */
    private String readTargetCell(
            XMLStreamReader reader,
            String sheetName)
            throws XMLStreamException, IOException {
        var logicalRow = 0;

        while (reader.hasNext()) {
            var event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT
                    && isTableElement(reader, "table-row")) {
                var repeatedRows = repeatedCount(
                        reader,
                        "number-rows-repeated");

                var lastRow = logicalRow + repeatedRows;

                if (TARGET_ROW <= lastRow) {
                    return readCellFromRow(reader, sheetName);
                }

                skipElement(reader);
                logicalRow = lastRow;
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && isTableElement(reader, "table")) {
                break;
            }
        }

        throw new IOException(
                "Worksheet '%s' does not contain row %d"
                        .formatted(sheetName, TARGET_ROW));
    }

    /**
     * Reads the logical third cell from the current row.
     *
     * @param reader    XML reader positioned at a row element
     * @param sheetName worksheet name
     * @return cached value from cell C13
     * @throws XMLStreamException if XML parsing fails
     * @throws IOException        if the target cell is missing
     */
    private String readCellFromRow(
            XMLStreamReader reader,
            String sheetName)
            throws XMLStreamException, IOException {
        var logicalColumn = 0;

        while (reader.hasNext()) {
            var event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT
                    && (isTableElement(reader, "table-cell")
                    || isTableElement(reader, "covered-table-cell"))) {
                var repeatedColumns = repeatedCount(
                        reader,
                        "number-columns-repeated");

                var lastColumn = logicalColumn + repeatedColumns;

                if (TARGET_COLUMN <= lastColumn) {
                    return readCellValue(reader);
                }

                skipElement(reader);
                logicalColumn = lastColumn;
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && isTableElement(reader, "table-row")) {
                break;
            }
        }

        throw new IOException(
                "Worksheet '%s' does not contain cell C%d"
                        .formatted(sheetName, TARGET_ROW));
    }

    /**
     * Reads the cached value of the current ODS table cell.
     *
     * @param reader XML reader positioned at a table cell
     * @return cell value
     * @throws XMLStreamException if XML parsing fails
     */
    private String readCellValue(XMLStreamReader reader)
            throws XMLStreamException {
        var stringValue = reader.getAttributeValue(
                OFFICE_NAMESPACE,
                "string-value");

        var genericValue = reader.getAttributeValue(
                OFFICE_NAMESPACE,
                "value");

        var text = new StringBuilder();

        while (reader.hasNext()) {
            var event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS
                    || event == XMLStreamConstants.CDATA) {
                if (isInsideTextParagraph(reader)) {
                    text.append(reader.getText());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && (isTableElement(reader, "table-cell")
                    || isTableElement(reader, "covered-table-cell"))) {
                break;
            }
        }

        if (stringValue != null) {
            return stringValue;
        }

        if (genericValue != null) {
            return genericValue;
        }

        return text.toString();
    }

    /**
     * Determines whether the reader is currently inside text content.
     *
     * @param reader XML stream reader
     * @return {@code true} when character data belongs to a text element
     */
    private boolean isInsideTextParagraph(XMLStreamReader reader) {
        return reader.hasName()
                && TEXT_NAMESPACE.equals(reader.getNamespaceURI());
    }

    /**
     * Reads an ODF repetition count, defaulting to one.
     *
     * @param reader        XML stream reader
     * @param attributeName local attribute name
     * @return logical repetition count
     */
    private int repeatedCount(
            XMLStreamReader reader,
            String attributeName) {
        var value = reader.getAttributeValue(
                TABLE_NAMESPACE,
                attributeName);

        return value == null ? 1 : Integer.parseInt(value);
    }

    /**
     * Skips the current XML element and all of its descendants.
     *
     * @param reader XML reader positioned at a start element
     * @throws XMLStreamException if XML parsing fails
     */
    private void skipElement(XMLStreamReader reader)
            throws XMLStreamException {
        var depth = 1;

        while (depth > 0 && reader.hasNext()) {
            var event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    /**
     * Checks whether the current element belongs to the ODF table namespace.
     *
     * @param reader    XML stream reader
     * @param localName expected local name
     * @return {@code true} if the current element matches
     */
    private boolean isTableElement(
            XMLStreamReader reader,
            String localName) {
        return localName.equals(reader.getLocalName())
                && TABLE_NAMESPACE.equals(reader.getNamespaceURI());
    }
}