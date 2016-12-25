package ch.illubits.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean containsOnlyDigits(String str) {
        return !isNullOrEmpty(str) && str.matches("[0-9]+");
    }

    public static boolean isNullOrEmptyTrimify(String string) {
        return isNullOrEmpty(trimNullSafe(string));
    }

    public static String trimNullSafe(String string) {
        if (string != null) {
            return string.trim();
        }
        return null;
    }

    public static String zeroPadNumber(long number, int length) {
        String result = String.valueOf(number);
        while (result.length() < length) {
            result = "0" + result;
        }
        return result;
    }

    public static boolean patternMatches(Pattern pattern, String str) {
        return pattern.matcher(str).find();
    }

    public static boolean anyPatternMatches(List<Pattern> patterns, String str) {
        return patterns.stream().anyMatch(p -> patternMatches(p, str));
    }

    public static String extractFirstGroup(Pattern pattern, String str) {
        return extractGroup(pattern, str, 1);
    }

    public static List<String> extractFirstGroupFromPatterns(List<Pattern> patterns, String str) {
        List<String> results = new ArrayList<>();
        for (Pattern pattern : patterns) {
            if (patternMatches(pattern, str)) {
                results.add(extractFirstGroup(pattern, str));
            }
        }
        return results;
    }

    public static String extractGroup(Pattern pattern, String str, int group) {
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            return m.group(group);
        } else {
            throw new IllegalStateException("Pattern did not match.");
        }
    }
}
