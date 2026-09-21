package org.example.data;

import org.example.model.Card;
import org.example.model.Lesson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LessonLoader {

    public static final String DEFAULT_RESOURCE = "/lessons.txt";

    private LessonLoader() {
    }

    public static List<Lesson> loadDefault() {
        InputStream stream = LessonLoader.class.getResourceAsStream(DEFAULT_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Не найден файл уроков: " + DEFAULT_RESOURCE);
        }
        try (stream) {
            return parse(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<Lesson> loadFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parse(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<Lesson> parse(BufferedReader reader) throws IOException {
        List<Lesson> lessons = new ArrayList<>();
        String title = null;
        List<Card> cards = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("##")) {
                flush(lessons, title, cards);
                title = line.replaceFirst("^#+\\s*", "").trim();
                cards = new ArrayList<>();
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            int split = line.indexOf('|');
            if (split < 0) {
                throw new IllegalArgumentException("Строка без разделителя '|': " + line);
            }
            String english = line.substring(0, split).trim();
            String russian = line.substring(split + 1).trim();
            if (english.isEmpty() || russian.isEmpty()) {
                throw new IllegalArgumentException("Пустое выражение или перевод: " + line);
            }
            if (title == null) {
                title = "Без названия";
            }
            cards.add(new Card(english, russian));
        }
        flush(lessons, title, cards);
        if (lessons.isEmpty()) {
            throw new IllegalStateException("В файле нет ни одной карточки");
        }
        return List.copyOf(lessons);
    }

    private static void flush(List<Lesson> lessons, String title, List<Card> cards) {
        if (title == null || cards.isEmpty()) {
            return;
        }
        lessons.add(new Lesson(title, List.copyOf(cards)));
    }
}
