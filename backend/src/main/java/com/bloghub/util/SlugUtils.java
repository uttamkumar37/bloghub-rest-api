package com.bloghub.util;

import java.text.Normalizer;

public final class SlugUtils {
    private SlugUtils() {}

    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }
        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return slug.isBlank() ? null : slug;
    }
}

