package org.example.quiz;

import org.example.model.Card;

public record AttemptResult(
        Kind kind,
        CheckResult check,
        String typed,
        Card card,
        Card otherMatch,
        String hint
) {
    public enum Kind {
        EMPTY,
        HINT,
        SKIP,
        CHECKED,
        QUIT
    }

    public static AttemptResult empty() {
        return new AttemptResult(Kind.EMPTY, null, "", null, null, null);
    }

    public static AttemptResult hint(String hint) {
        return new AttemptResult(Kind.HINT, null, "", null, null, hint);
    }

    public static AttemptResult quit() {
        return new AttemptResult(Kind.QUIT, null, "", null, null, null);
    }

    public static AttemptResult skipped(Card card) {
        return new AttemptResult(Kind.SKIP, new CheckResult(Verdict.WRONG, Integer.MAX_VALUE, 0), "", card, null, null);
    }

    public static AttemptResult checked(CheckResult check, String typed, Card card, Card otherMatch) {
        return new AttemptResult(Kind.CHECKED, check, typed, card, otherMatch, null);
    }
}
