package com.infoyupay.mtcexpediente41.datasheet;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link VehicleIdentifierReaders}.
 * <br/>
 *  <div data-infoyupay="manual-test-verification"
 *    style="border:1px solid #c00;
 *    border-radius:2px;
 *    padding:4px;
 *    color:#c00;
 *    margin-top:6px;
 *    margin-bottom:6px;">
 *       <strong>Tested-by:</strong>
 *       dvidal@infoyupay.com - passed 4 tests in 1.876s at 2026-07-24T23:57:17 (UTC-5).
 * </div>
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class VehicleIdentifierReadersTest {

    /**
     * Verifies that XLSX workbooks select the XLSX reader.
     */
    @Test
    void shouldSelectXlsxReader() {
        var reader =
                VehicleIdentifierReaders.forWorkbook(
                        Path.of("Fichas.xlsx"));

        assertThat(reader)
                .isInstanceOf(XlsxVehicleIdentifierReader.class);
    }

    /**
     * Verifies that ODS workbooks select the ODS reader.
     */
    @Test
    void shouldSelectOdsReader() {
        var reader =
                VehicleIdentifierReaders.forWorkbook(
                        Path.of("Fichas.ods"));

        assertThat(reader)
                .isInstanceOf(OdsVehicleIdentifierReader.class);
    }

    /**
     * Verifies that extension matching is case-insensitive.
     */
    @Test
    void shouldIgnoreExtensionCase() {
        var reader =
                VehicleIdentifierReaders.forWorkbook(
                        Path.of("FICHAS.XLSX"));

        assertThat(reader)
                .isInstanceOf(XlsxVehicleIdentifierReader.class);
    }

    /**
     * Verifies that unsupported workbook formats are rejected.
     */
    @Test
    void shouldRejectUnsupportedFormat() {
        var workbook = Path.of("Fichas.xls");

        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        VehicleIdentifierReaders.forWorkbook(workbook))
                .withMessageContaining(
                        "Unsupported spreadsheet format:");
    }
}