package com.hostelmanagement.util;

import com.hostelmanagement.exception.ApiException;

/** Small validation helpers used by the service layer. */
public final class Check {

    private Check() { }

    /** Required text: must not be blank. Returns the trimmed value. */
    public static String text(String value, String label) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest(label + " is required");
        }
        return value.trim();
    }

    /** Optional text: returns trimmed value or null when blank. */
    public static String optional(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public static <E extends Enum<E>> E enumValue(Class<E> type, String value, String label) {
        try {
            return Enum.valueOf(type, text(value, label).toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Invalid " + label + ": " + value);
        }
    }
}
