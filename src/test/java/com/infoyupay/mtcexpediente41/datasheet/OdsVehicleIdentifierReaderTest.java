package com.infoyupay.mtcexpediente41.datasheet;

import com.infoyupay.mtcexpediente41.samples.VehicleIdentifiers;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OdsVehicleIdentifierReader}.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class OdsVehicleIdentifierReaderTest {

    /**
     * Verifies that vehicle identifiers are read from cell C13 of every
     * worksheet in the sample ODS workbook.
     *
     * @throws Exception if the test workbook cannot be read
     */
    @Test
    void shouldReadVehicleIdentifiersFromEveryWorksheet()
            throws Exception {
        var workbook = resourcePath(
                "/com/infoyupay/mtcexpediente41/pdf-samples/Fichas.ods");

        var identifiers =
                new OdsVehicleIdentifierReader().read(workbook);
        identifiers.forEach(System.out::println);

        var actualIdentifiers = identifiers.stream()
                .collect(Collectors.toMap(
                        SheetVehicleIdentifier::sheetName,
                        SheetVehicleIdentifier::vehicleIdentifier));

        assertThat(identifiers)
                .hasSize(VehicleIdentifiers.IDENTIFIER_BY_SHEET_NAME.size());

        assertThat(actualIdentifiers)
                .containsExactlyInAnyOrderEntriesOf(
                        VehicleIdentifiers.IDENTIFIER_BY_SHEET_NAME);
    }

    /**
     * Resolves a classpath resource as a local path.
     *
     * @param resourceName absolute classpath resource name
     * @return resolved resource path
     * @throws URISyntaxException if the URI is invalid
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