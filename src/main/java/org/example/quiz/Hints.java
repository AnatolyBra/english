package org.example.quiz;

public final class Hints {

    private Hints() {
    }

    public static String mask(String english) {
        String[] words = english.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                out.append(' ');
            }
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }
            out.append(word.charAt(0));
            for (int j = 1; j < word.length(); j++) {
                char ch = word.charAt(j);
                out.append(Character.isLetterOrDigit(ch) ? '_' : ch);
            }
        }
        return out.toString();
    }
}
