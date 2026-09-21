package org.example.quiz;

public record CheckResult(Verdict verdict, int distance, double similarity) {
    public boolean accepted() {
        return verdict == Verdict.CORRECT || verdict == Verdict.CLOSE;
    }
}
