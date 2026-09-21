package org.example.quiz;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HintsTest {

    @Test
    void maskKeepsFirstLettersAndPunctuation() {
        assertEquals(
                "W'__ g__ a_ i____ w___...",
                Hints.mask("We've got an issue with...")
        );
    }
}
