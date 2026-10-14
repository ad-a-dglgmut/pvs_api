package com.parkeye.pvs.util;

import com.parkeye.pvs.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlateUtil {
    // Macedonian plates: 2 letters (city), 3-4 digits, 2 letters. e.g. SR-1234-AB
    private static final Pattern PLATE = Pattern.compile("^([A-Z]{2})[- ]?(\\d{3,4})[- ]?([A-Z]{2})$");

    private PlateUtil() {}

    /** Trims, upper-cases and formats as XX-NNNN-XX. Throws INVALID_PLATE if it doesn't fit. */
    public static String normalize(String raw) {
        if (raw == null) throw invalid();
        Matcher m = PLATE.matcher(raw.trim().toUpperCase());
        if (!m.matches()) throw invalid();
        return m.group(1) + "-" + m.group(2) + "-" + m.group(3);
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PLATE", "License plate must look like SR-1234-AB.");
    }
}
