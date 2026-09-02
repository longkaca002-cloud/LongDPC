package com.longkaca.ocr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmailExtractor {
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPACED_EMAIL = Pattern.compile(
            "([A-Z0-9._%+-]+)\\s*@\\s*([A-Z0-9-]+(?:\\s*\\.\\s*[A-Z0-9-]+)+)",
            Pattern.CASE_INSENSITIVE);

    private EmailExtractor() {}

    public static String firstEmail(String text) {
        if (text == null) return "";
        Matcher direct = EMAIL.matcher(text);
        if (direct.find()) return clean(direct.group());

        Matcher spaced = SPACED_EMAIL.matcher(text);
        if (spaced.find()) {
            String candidate = spaced.group(1) + "@" + spaced.group(2).replaceAll("\\s+", "");
            Matcher verify = EMAIL.matcher(candidate);
            if (verify.matches()) return clean(candidate);
        }
        return "";
    }

    private static String clean(String value) {
        return value.replace('\u3002', '.').trim();
    }
}
