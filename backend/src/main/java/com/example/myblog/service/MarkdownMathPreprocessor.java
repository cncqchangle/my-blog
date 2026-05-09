package com.example.myblog.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MarkdownMathPreprocessor {

    private static final Pattern FENCE_PATTERN = Pattern.compile("^\\s*([`~]{3,}).*$");

    private MarkdownMathPreprocessor() {
    }

    static String preprocess(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String normalized = markdown.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        StringBuilder result = new StringBuilder();
        FenceState fenceState = null;

        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];

            if (fenceState != null) {
                appendLine(result, line, index < lines.length - 1);
                if (isClosingFence(line, fenceState)) {
                    fenceState = null;
                }
                continue;
            }

            FenceState detectedFence = detectFence(line);
            if (detectedFence != null) {
                fenceState = detectedFence;
                appendLine(result, line, index < lines.length - 1);
                continue;
            }

            if (isBlockMathDelimiter(line)) {
                String trimmed = line.trim();
                if (trimmed.length() > 4 && trimmed.endsWith("$$")) {
                    String expression = trimmed.substring(2, trimmed.length() - 2).trim();
                    appendLine(result, toMathSpan(expression, true), index < lines.length - 1);
                    continue;
                }

                int closingIndex = findClosingBlockMath(lines, index + 1);
                if (closingIndex != -1) {
                    String expression = joinLines(lines, index + 1, closingIndex).trim();
                    appendLine(result, toMathSpan(expression, true), closingIndex < lines.length - 1);
                    index = closingIndex;
                    continue;
                }
            }

            appendLine(result, processInlineMath(line), index < lines.length - 1);
        }

        return result.toString();
    }

    private static String processInlineMath(String line) {
        StringBuilder result = new StringBuilder();
        int index = 0;

        while (index < line.length()) {
            char current = line.charAt(index);

            if (current == '\\') {
                if (index + 1 < line.length()) {
                    result.append(current).append(line.charAt(index + 1));
                    index += 2;
                } else {
                    result.append(current);
                    index++;
                }
                continue;
            }

            if (current == '`') {
                int tickCount = countRepeated(line, index, '`');
                int closingIndex = findBacktickClose(line, index + tickCount, tickCount);
                if (closingIndex != -1) {
                    result.append(line, index, closingIndex + tickCount);
                    index = closingIndex + tickCount;
                    continue;
                }
            }

            if (current == '$' && !isDoubleDollar(line, index)) {
                int closingIndex = findInlineMathClose(line, index + 1);
                if (closingIndex != -1) {
                    String expression = line.substring(index + 1, closingIndex);
                    if (isValidInlineExpression(expression)) {
                        result.append(toMathSpan(expression, false));
                        index = closingIndex + 1;
                        continue;
                    }
                }
            }

            result.append(current);
            index++;
        }

        return result.toString();
    }

    private static boolean isDoubleDollar(String line, int index) {
        return index + 1 < line.length() && line.charAt(index + 1) == '$';
    }

    private static int findInlineMathClose(String line, int startIndex) {
        int index = startIndex;
        while (index < line.length()) {
            char current = line.charAt(index);
            if (current == '\\') {
                index += 2;
                continue;
            }
            if (current == '`') {
                return -1;
            }
            if (current == '$' && !isDoubleDollar(line, index)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static boolean isValidInlineExpression(String expression) {
        if (expression.isBlank()) {
            return false;
        }
        return !Character.isWhitespace(expression.charAt(0))
                && !Character.isWhitespace(expression.charAt(expression.length() - 1));
    }

    private static int findBacktickClose(String line, int startIndex, int tickCount) {
        for (int index = startIndex; index < line.length(); index++) {
            if (line.charAt(index) != '`') {
                continue;
            }
            int repeated = countRepeated(line, index, '`');
            if (repeated == tickCount) {
                return index;
            }
        }
        return -1;
    }

    private static int countRepeated(String source, int startIndex, char marker) {
        int count = 0;
        while (startIndex + count < source.length() && source.charAt(startIndex + count) == marker) {
            count++;
        }
        return count;
    }

    private static boolean isBlockMathDelimiter(String line) {
        return line.trim().startsWith("$$");
    }

    private static int findClosingBlockMath(String[] lines, int startIndex) {
        for (int index = startIndex; index < lines.length; index++) {
            if (lines[index].trim().equals("$$")) {
                return index;
            }
        }
        return -1;
    }

    private static String joinLines(String[] lines, int startInclusive, int endExclusive) {
        StringBuilder result = new StringBuilder();
        for (int index = startInclusive; index < endExclusive; index++) {
            if (index > startInclusive) {
                result.append('\n');
            }
            result.append(lines[index]);
        }
        return result.toString();
    }

    private static FenceState detectFence(String line) {
        Matcher matcher = FENCE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        String marker = matcher.group(1);
        return new FenceState(marker.charAt(0), marker.length());
    }

    private static boolean isClosingFence(String line, FenceState fenceState) {
        String trimmed = line.stripLeading();
        int count = countRepeated(trimmed, 0, fenceState.marker());
        return count >= fenceState.length();
    }

    private static String toMathSpan(String expression, boolean displayMode) {
        return "<span class=\"" + (displayMode ? "math-display" : "math-inline") + "\">"
                + escapeHtml(expression)
                + "</span>";
    }

    private static String escapeHtml(String expression) {
        StringBuilder result = new StringBuilder(expression.length());
        for (char current : expression.toCharArray()) {
            switch (current) {
                case '&' -> result.append("&amp;");
                case '<' -> result.append("&lt;");
                case '>' -> result.append("&gt;");
                case '"' -> result.append("&quot;");
                case '\'' -> result.append("&#39;");
                default -> result.append(current);
            }
        }
        return result.toString();
    }

    private static void appendLine(StringBuilder target, String line, boolean appendNewline) {
        target.append(line);
        if (appendNewline) {
            target.append('\n');
        }
    }

    private record FenceState(char marker, int length) {
    }
}
