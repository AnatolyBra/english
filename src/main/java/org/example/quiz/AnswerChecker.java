package org.example.quiz;

import java.util.Locale;

/**
 * Checks typed English against the expected phrase.
 * Same idea as CLI trainers like QUIZR: ignore case/punctuation,
 * then accept a small typo via Levenshtein distance.
 */
public final class AnswerChecker {

    private static final int MAX_CLOSE_DISTANCE = 2;
    private static final double CLOSE_SIMILARITY = 0.90;

    public CheckResult check(String expected, String given) {
        String left = normalize(expected);
        String right = normalize(given);
        if (left.isEmpty() || right.isEmpty()) {
            return new CheckResult(Verdict.WRONG, Integer.MAX_VALUE, 0);
        }
        if (left.equals(right)) {
            return new CheckResult(Verdict.CORRECT, 0, 1.0);
        }
        int distance = levenshtein(left, right);
        int longest = Math.max(left.length(), right.length());
        double similarity = 1.0 - (double) distance / longest;
        Verdict verdict = (distance <= MAX_CLOSE_DISTANCE || similarity >= CLOSE_SIMILARITY)
                ? Verdict.CLOSE
                : Verdict.WRONG;
        return new CheckResult(verdict, distance, similarity);
    }

    public boolean matches(String expected, String given) {
        return check(expected, given).accepted();
    }

    static String normalize(String raw) {
        String text = raw.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('`', '\'')
                .replace("…", " ");
        StringBuilder out = new StringBuilder(text.length());
        boolean space = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '\'') {
                out.append(ch);
                space = false;
            } else if (!space) {
                out.append(' ');
                space = true;
            }
        }
        return out.toString().trim();
    }

    static int levenshtein(String left, String right) {
        int[] prev = new int[right.length() + 1];
        int[] curr = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            curr[0] = i;
            char a = left.charAt(i - 1);
            for (int j = 1; j <= right.length(); j++) {
                int cost = a == right.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] swap = prev;
            prev = curr;
            curr = swap;
        }
        return prev[right.length()];
    }
}
