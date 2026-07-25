package com.infoyupay.mtcexpediente41.datasheet;

import com.infoyupay.mtcexpediente41.samples.VehicleIdentifiers;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XlsxVehicleIdentifierReader}.
 * <div data-infoyupay="manual-test-verification"
 * style="border:1px solid #c00;
 * border-radius:2px;
 * padding:4px;
 * color:#c00;
 * margin-top:6px;
 * margin-bottom:6px;">
 * <strong>Tested-by:</strong>
 * dvidal@infoyupay.com - passed 1 tests in 2.347s at 2026-07-24T23:38:12 (UTC-5).
 * </div>
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class XlsxVehicleIdentifierReaderTest {

    /**
     * Verifies that the reader obtains the cached value of cell C13 from every
     * worksheet in the sample XLSX workbook.
     *
     * @throws Exception if the test resource cannot be read
     */
    @Test
    void shouldReadVehicleIdentifiersFromEveryWorksheet()
            throws Exception {
        var workbook = resourcePath(
                "/com/infoyupay/mtcexpediente41/pdf-samples/Fichas.xlsx");

        var reader = new XlsxVehicleIdentifierReader();

        var identifiers = reader.read(workbook);

        identifiers.forEach(System.out::println);

        var actualIdentifiers = identifiers.stream()
                .collect(Collectors.toMap(
                        SheetVehicleIdentifier::sheetName,
                        SheetVehicleIdentifier::vehicleIdentifier));

        assertThat(actualIdentifiers)
                .containsExactlyInAnyOrderEntriesOf(
                        VehicleIdentifiers.IDENTIFIER_BY_SHEET_NAME);
    }

    /**
     * Resolves a classpath resource as a local file-system path.
     *
     * @param resourceName absolute classpath resource name
     * @return path of the resolved resource
     * @throws URISyntaxException if the resource URI cannot be converted
     */
    private Path resourcePath(@SuppressWarnings("SameParameterValue") String resourceName)
            throws URISyntaxException {
        var resource = getClass().getResource(resourceName);

        assertThat(resource)
                .as("Test resource %s", resourceName)
                .isNotNull();

        return Path.of(resource.toURI());
    }
}