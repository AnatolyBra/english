package org.example.quiz;

import org.example.model.Card;

import java.util.List;

public record RoundResult(int total, int accepted, List<Card> missed, List<Mistake> mistakes) {
}
