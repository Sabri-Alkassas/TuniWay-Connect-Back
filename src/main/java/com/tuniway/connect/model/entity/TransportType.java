package com.tuniway.connect.model.entity;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public enum TransportType {
    BUS,
    TRAIN,
    METRO;

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Z0-9]+");

    public static TransportType fromExternalValue(String rawValue) {
        String normalized = normalize(rawValue);
        if (normalized == null) {
            return null;
        }

        return switch (normalized) {
            case "BUS" -> BUS;
            case "TRAIN", "RAIL" -> TRAIN;
            case "METRO", "TRAM", "TRAMWAY", "SUBWAY", "LIGHT_RAIL", "LIGHTRAIL", "METRO_LEGER", "TGM" -> METRO;
            default -> null;
        };
    }

    private static String normalize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String asciiValue = COMBINING_MARKS
            .matcher(Normalizer.normalize(rawValue, Normalizer.Form.NFD))
            .replaceAll("");

        String normalized = NON_ALPHANUMERIC
            .matcher(asciiValue.trim().toUpperCase(Locale.ENGLISH))
            .replaceAll("_")
            .replaceAll("_+", "_");

        return normalized.isBlank() ? null : normalized;
    }
}