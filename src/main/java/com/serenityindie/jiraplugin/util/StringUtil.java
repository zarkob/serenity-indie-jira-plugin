package com.serenityindie.jiraplugin.util;

import java.util.Set;

public class StringUtil {

    static Set<Character> specialChars = Set.of('"', '\\', '/', '\b', '\f', '\n', '\r', '\t');

    public static String escapeSpecialCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder escapedString = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (specialChars.contains(c)) {
                escapedString.append("\\"); // Append the escape character
            }
            escapedString.append(c);
        }
        return escapedString.toString();
    }
}
