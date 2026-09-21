package org.example.quiz;

import org.example.model.Card;
import org.example.model.Lesson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuizSession {

    public enum Phase {
        ASKING,
        FEEDBACK,
        DONE
    }

    private final Lesson lesson;
    private final AnswerChecker checker;
    private final List<Card> deck;
    private final boolean reviewMode;
    private final List<Mistake> mistakes = new ArrayList<>();
    private final List<Card> missed = new ArrayList<>();

    private int index;
    private int accepted;
    private Phase phase = Phase.ASKING;

    public QuizSession(Lesson lesson, List<Card> cards, boolean reviewMode) {
        this.lesson = lesson;
        this.checker = new AnswerChecker();
        this.deck = new ArrayList<>(cards);
        Collections.shuffle(this.deck);
        this.reviewMode = reviewMode;
        if (this.deck.isEmpty()) {
            this.phase = Phase.DONE;
        }
    }

    public static QuizSession firstPass(Lesson lesson) {
        return new QuizSession(lesson, lesson.cards(), false);
    }

    public static QuizSession review(Lesson lesson, List<Card> missedCards) {
        return new QuizSession(lesson, missedCards, true);
    }

    public Lesson lesson() {
        return lesson;
    }

    public boolean reviewMode() {
        return reviewMode;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isAsking() {
        return phase == Phase.ASKING;
    }

    public boolean isFeedback() {
        return phase == Phase.FEEDBACK;
    }

    public boolean isDone() {
        return phase == Phase.DONE;
    }

    public Card current() {
        if (phase == Phase.DONE) {
            throw new IllegalStateException("Раунд уже закончен");
        }
        return deck.get(index);
    }

    public int position() {
        return index + 1;
    }

    public int size() {
        return deck.size();
    }

    public AttemptResult attempt(String raw) {
        if (phase != Phase.ASKING) {
            throw new IllegalStateException("Сейчас не время для ответа");
        }
        String typed = raw == null ? "" : raw.trim();
        if (Commands.isQuit(typed)) {
            return AttemptResult.quit();
        }
        if (typed.isEmpty()) {
            return AttemptResult.empty();
        }
        if (Commands.isHint(typed)) {
            return AttemptResult.hint(Hints.mask(current().english()));
        }
        if (Commands.isSkip(typed)) {
            missed.add(current());
            mistakes.add(new Mistake(current(), ""));
            phase = Phase.FEEDBACK;
            return AttemptResult.skipped(current());
        }
        CheckResult check = checker.check(current().english(), typed);
        Card other = findOther(typed);
        if (check.accepted()) {
            accepted++;
        } else {
            missed.add(current());
            mistakes.add(new Mistake(current(), typed));
        }
        phase = Phase.FEEDBACK;
        return AttemptResult.checked(check, typed, current(), other);
    }

    public void advance() {
        if (phase != Phase.FEEDBACK) {
            return;
        }
        index++;
        phase = index >= deck.size() ? Phase.DONE : Phase.ASKING;
    }

    public RoundResult result() {
        return new RoundResult(deck.size(), accepted, List.copyOf(missed), List.copyOf(mistakes));
    }

    private Card findOther(String typed) {
        if (typed.isBlank()) {
            return null;
        }
        Card current = current();
        for (Card card : lesson.cards()) {
            if (card != current && checker.matches(card.english(), typed)) {
                return card;
            }
        }
        return null;
    }
}
