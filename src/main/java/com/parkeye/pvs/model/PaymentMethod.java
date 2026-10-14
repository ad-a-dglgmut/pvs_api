package com.parkeye.pvs.model;

import com.parkeye.pvs.exception.ApiException;
import org.springframework.http.HttpStatus;

public enum PaymentMethod {
    CARD,
    SMS,
    WALLET;

    /** Case-insensitive parse; null/blank defaults to CARD (what the prototype preselects). */
    public static PaymentMethod parse(String raw) {
        if (raw == null || raw.isBlank()) return CARD;
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_METHOD",
                    "Unknown payment method: " + raw);
        }
    }
}
