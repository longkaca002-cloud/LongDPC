package com.longkaca.ocr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EmailExtractor {
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPACED_EMAIL = Pattern.compile(
            "([A-Z0-9._%+-]+)\\s*@\\s*([A-Z0-9-]+(?:\\s*\\.\\s*[A-Z0-9-]+)+)",
            Pattern.CASE_INSENSITIVE);

    private EmailExtractor() {}

    public static String firstEmail(String text) {
        List<String> all = allEmails(text);
        return all.isEmpty() ? "" : all.get(0);
    }

    public static List<String> allEmails(String text) {
        Set<String> found = new LinkedHashSet<>();
        if (text == null) return new ArrayList<>();
        Matcher direct = EMAIL.matcher(text);
        while (direct.find()) found.add(clean(direct.group()));

        Matcher spaced = SPACED_EMAIL.matcher(text);
        while (spaced.find()) {
            String candidate = spaced.group(1) + "@" + spaced.group(2).replaceAll("\\s+", "");
            Matcher verify = EMAIL.matcher(candidate);
            if (verify.matches()) found.add(clean(candidate));
        }
        return new ArrayList<>(found);
    }

    private static String clean(String value) {
        return value.replace('\u3002', '.').trim();
    }
}
