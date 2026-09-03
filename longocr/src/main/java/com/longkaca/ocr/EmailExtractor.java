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
    private static final Pattern US_EMAIL = Pattern.compile(
            "[A-Z0-9._%+-]+@[A-Z0-9-]+(?:\\.[A-Z0-9-]+)*\\.US(?=$|[^A-Z0-9.-])",
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

    /** Extract .us accounts in visual line order, joining an address split over up to 3 OCR lines. */
    public static List<String> usEmailsFromLines(List<String> lines) {
        Set<String> found = new LinkedHashSet<>();
        if (lines == null) return new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String first = compact(lines.get(i));
            boolean completeOnFirst = addUsMatches(first, found);
            if (completeOnFirst) continue;

            boolean canStartWrapped = first.contains("@");
            if (!canStartWrapped && i + 1 < lines.size()) {
                canStartWrapped = compact(lines.get(i + 1)).startsWith("@");
            }
            if (!canStartWrapped) continue;

            StringBuilder joined = new StringBuilder(first);
            for (int span = 1; span < 3 && i + span < lines.size(); span++) {
                joined.append(compact(lines.get(i + span)));
                if (addUsMatches(joined.toString(), found)) break;
            }
        }
        return new ArrayList<>(found);
    }

    private static boolean addUsMatches(String text, Set<String> found) {
        boolean any = false;
        Matcher matcher = US_EMAIL.matcher(text);
        while (matcher.find()) {
            found.add(clean(matcher.group()));
            any = true;
        }
        return any;
    }

    private static String compact(String value) {
        if (value == null) return "";
        return value.replace('＠', '@')
                .replace('．', '.')
                .replace('。', '.')
                .replace("\\u200B", "")
                .replaceAll("\\s+", "")
                .trim();
    }

    private static String clean(String value) {
        return value.replace('\u3002', '.').trim();
    }
}
