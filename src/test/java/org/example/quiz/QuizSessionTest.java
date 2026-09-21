package org.example.quiz;

import org.example.model.Card;
import org.example.model.Lesson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizSessionTest {

    private final Lesson lesson = new Lesson("Test", List.of(
            new Card("We've got an issue with...", "Представить баг"),
            new Card("The issue occurs when...", "Объяснить условия")
    ));

    @Test
    void hintDoesNotAdvance() {
        QuizSession session = new QuizSession(lesson, List.of(lesson.cards().getFirst()), false);
        AttemptResult result = session.attempt("?");
        assertEquals(AttemptResult.Kind.HINT, result.kind());
        assertTrue(session.isAsking());
        assertEquals("W'__ g__ a_ i____ w___...", result.hint());
    }

    @Test
    void correctAnswerGoesToFeedback() {
        QuizSession session = new QuizSession(lesson, List.of(lesson.cards().getFirst()), false);
        AttemptResult result = session.attempt("we've got an issue with");
        assertEquals(AttemptResult.Kind.CHECKED, result.kind());
        assertTrue(result.check().accepted());
        assertTrue(session.isFeedback());
        session.advance();
        assertTrue(session.isDone());
        assertEquals(1, session.result().accepted());
    }

    @Test
    void detectsPhraseFromAnotherCard() {
        QuizSession session = new QuizSession(lesson, List.of(lesson.cards().getFirst()), false);
        AttemptResult result = session.attempt("The issue occurs when...");
        assertEquals(AttemptResult.Kind.CHECKED, result.kind());
        assertEquals(lesson.cards().get(1), result.otherMatch());
    }

    @Test
    void skipRecordsMissWithoutOtherMatch() {
        QuizSession session = new QuizSession(lesson, List.of(lesson.cards().getFirst()), false);
        AttemptResult result = session.attempt("s");
        assertEquals(AttemptResult.Kind.SKIP, result.kind());
        assertNull(result.otherMatch());
        assertEquals(1, session.result().missed().size());
    }
}
