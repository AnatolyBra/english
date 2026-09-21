package org.example.quiz;

public final class Commands {

    private Commands() {
    }

    public static boolean isQuit(String raw) {
        String value = raw.trim().toLowerCase();
        return value.equals("q") || value.equals("quit") || value.equals("выход");
    }

    public static boolean isSkip(String raw) {
        String value = raw.trim().toLowerCase();
        return value.equals("s") || value.equals("skip") || value.equals("пропуск");
    }

    public static boolean isHint(String raw) {
        String value = raw.trim();
        return value.equals("?") || value.equalsIgnoreCase("hint") || value.equalsIgnoreCase("подсказка");
    }

    public static boolean isYes(String answer) {
        return answer.equals("д") || answer.equals("да") || answer.equals("y") || answer.equals("yes");
    }

    public static boolean isNo(String answer) {
        return answer.equals("н") || answer.equals("нет") || answer.equals("n") || answer.equals("no");
    }
}
