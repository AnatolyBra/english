package org.example.quiz;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswerCheckerTest {

    private final AnswerChecker checker = new AnswerChecker();

    @Test
    void acceptsSamePhraseIgnoringCasePunctuationAndEllipsis() {
        CheckResult result = checker.check(
                "We've got an issue with...",
                "we've got an issue with"
        );
        assertEquals(Verdict.CORRECT, result.verdict());
    }

    @Test
    void acceptsTrailingPeriodVariant() {
        assertTrue(checker.matches("I was able to reproduce it.", "I was able to reproduce it"));
    }

    @Test
    void treatsSmallTypoAsClose() {
        CheckResult result = checker.check(
                "It happens intermittently.",
                "It happens intermittantly."
        );
        assertEquals(Verdict.CLOSE, result.verdict());
        assertTrue(result.accepted());
    }

    @Test
    void rejectsDifferentPhrase() {
        CheckResult result = checker.check(
                "The expected behavior is...",
                "What actually happens is..."
        );
        assertEquals(Verdict.WRONG, result.verdict());
        assertFalse(result.accepted());
    }

    @Test
    void emptyAnswerIsWrong() {
        assertFalse(checker.matches("This could affect...", "   "));
    }
}
