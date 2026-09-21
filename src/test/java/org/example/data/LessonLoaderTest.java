package org.example.data;

import org.example.model.Lesson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LessonLoaderTest {

    @Test
    void loadsBundledLesson() {
        List<Lesson> lessons = LessonLoader.loadDefault();
        assertEquals(15, lessons.size());

        Lesson core = lessons.getFirst();
        assertEquals("Lesson 3: Core Chunks", core.title());
        assertEquals(10, core.cards().size());
        assertEquals("We've got an issue with...", core.cards().getFirst().english());
        assertEquals("У нас проблема с...", core.cards().getFirst().russian());

        Lesson chunks = byTitle(lessons, "Survive the Stand-Up · Chunks");
        assertEquals(73, chunks.cards().size());
        assertTrue(chunks.cards().stream().anyMatch(card -> card.english().equals("come up with")));
        assertTrue(chunks.cards().stream().anyMatch(card -> card.english().equals("drill down into")));
        assertEquals(
                "Придумать / предложить",
                chunks.cards().stream()
                        .filter(card -> card.english().equals("come up with"))
                        .findFirst()
                        .orElseThrow()
                        .russian()
        );
    }

    private static Lesson byTitle(List<Lesson> lessons, String title) {
        return lessons.stream()
                .filter(lesson -> lesson.title().equals(title))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Нет пачки: " + title));
    }
}
