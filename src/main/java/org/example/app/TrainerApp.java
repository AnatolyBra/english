package org.example.app;

import org.example.data.LessonLoader;
import org.example.model.Card;
import org.example.model.Lesson;
import org.example.quiz.AttemptResult;
import org.example.quiz.Commands;
import org.example.quiz.Mistake;
import org.example.quiz.QuizSession;
import org.example.quiz.RoundResult;
import org.example.quiz.Verdict;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class TrainerApp {

    private final Scanner in;
    private final PrintStream out;

    public TrainerApp() {
        this(new Scanner(System.in, StandardCharsets.UTF_8), System.out);
    }

    TrainerApp(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            runSession();
        } catch (SessionAborted ignored) {
            out.println("Пока.");
        }
    }

    private void runSession() {
        List<Lesson> lessons = LessonLoader.loadDefault();
        out.println();
        out.println("Тренажёр выражений");
        out.println("Пиши английскую фразу и жми Enter.");
        out.println("Команды: q — выход, ? — подсказка, s — пропустить.");
        out.println();

        Lesson lesson = pickLesson(lessons);
        boolean again = true;
        while (again) {
            RoundResult firstPass = runRound(QuizSession.firstPass(lesson));
            printSummary(firstPass);
            if (!firstPass.missed().isEmpty() && askYesNo("Повторить ошибки?")) {
                List<Card> review = new ArrayList<>(firstPass.missed());
                while (!review.isEmpty()) {
                    RoundResult extra = runRound(QuizSession.review(lesson, review));
                    review = extra.missed();
                    if (!review.isEmpty() && !askYesNo("Ещё раз оставшиеся ошибки?")) {
                        break;
                    }
                }
            }
            again = askYesNo("Пройти пачку ещё раз?");
        }
        out.println("Пока.");
    }

    private Lesson pickLesson(List<Lesson> lessons) {
        if (lessons.size() == 1) {
            return lessons.getFirst();
        }
        out.println("Пачки:");
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            out.printf("  %d. %s (%d)%n", i + 1, lesson.title(), lesson.cards().size());
        }
        while (true) {
            out.print("Номер пачки: ");
            String raw = readLine();
            if (raw == null || Commands.isQuit(raw)) {
                throw new SessionAborted();
            }
            try {
                int index = Integer.parseInt(raw.trim());
                if (index >= 1 && index <= lessons.size()) {
                    return lessons.get(index - 1);
                }
            } catch (NumberFormatException ignored) {
                // ask again
            }
            out.println("Введи номер из списка.");
        }
    }

    private RoundResult runRound(QuizSession session) {
        out.println();
        out.println("── " + session.lesson().title() + (session.reviewMode() ? " · повтор ошибок" : "") + " ──");

        while (!session.isDone()) {
            out.println();
            out.printf("  %d / %d%n", session.position(), session.size());
            out.println("  " + session.current().russian());
            resolveCard(session);
            session.advance();
        }
        return session.result();
    }

    private void resolveCard(QuizSession session) {
        while (session.isAsking()) {
            out.print("  английский > ");
            String typed = readLine();
            if (typed == null) {
                throw new SessionAborted();
            }
            AttemptResult result = session.attempt(typed);
            switch (result.kind()) {
                case QUIT -> throw new SessionAborted();
                case EMPTY -> out.println("  Пустая строка не считается. Напиши фразу, s или q.");
                case HINT -> out.println("  подсказка: " + result.hint());
                case SKIP, CHECKED -> printAttempt(result);
            }
        }
    }

    private void printAttempt(AttemptResult result) {
        if (result.check() != null && result.check().accepted()) {
            if (result.check().verdict() == Verdict.CLOSE) {
                out.println("  ≈ Почти. Канон: " + result.card().english());
            } else {
                out.println("  ✓ Верно");
            }
            return;
        }
        out.println("  ✗ Неверно");
        out.println("    правильно: " + result.card().english());
        if (result.otherMatch() != null) {
            out.println("    ты ввёл фразу для: " + result.otherMatch().russian());
        }
    }

    private void printSummary(RoundResult result) {
        out.println();
        out.printf("Результат: %d / %d%n", result.accepted(), result.total());
        if (result.mistakes().isEmpty()) {
            out.println("Все верно.");
            return;
        }
        out.println("Ошибки:");
        for (Mistake mistake : result.mistakes()) {
            out.println("  · " + mistake.card().russian());
            if (!mistake.typed().isBlank()) {
                out.println("    ты: " + mistake.typed());
            }
            out.println("    надо: " + mistake.card().english());
        }
    }

    private boolean askYesNo(String question) {
        while (true) {
            out.print(question + " [yes/no] ");
            String raw = readLine();
            if (raw == null || Commands.isQuit(raw)) {
                throw new SessionAborted();
            }
            String answer = raw.trim().toLowerCase();
            if (answer.isEmpty()) {
                continue;
            }
            if (Commands.isYes(answer)) {
                return true;
            }
            if (Commands.isNo(answer)) {
                return false;
            }
            out.println("Type yes or no.");
        }
    }

    private String readLine() {
        if (!in.hasNextLine()) {
            return null;
        }
        return in.nextLine();
    }

    static final class SessionAborted extends RuntimeException {
        SessionAborted() {
            super("Выход");
        }
    }
}
