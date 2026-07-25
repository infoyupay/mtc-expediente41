package com.infoyupay.mtcexpediente41.samples;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * Provides the expected worksheet names and vehicle identifiers used by the
 * sample workbook test resources.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public final class VehicleIdentifiers {

    /**
     * Expected vehicle identifier indexed by worksheet name.
     */
    @Unmodifiable
    @NotNull
    public static final Map<@NotNull String, @NotNull String>
            IDENTIFIER_BY_SHEET_NAME = Map.of(
            "1001", "ABCDE1234WX001001",
            "1002", "ABCDE1234WX001002",
            "1003", "ABCDE1234WX001003",
            "1004", "ABCDE1234WX001004",
            "1005", "ABCDE1234WX001005",
            "1006", "BBCDE1234WX001006",
            "1007", "BBCDE1234WX001007",
            "1008", "CBCDE1234WX001008");

    /**
     * Prevents instantiation of this utility class.
     */
    private VehicleIdentifiers() {
    }
}