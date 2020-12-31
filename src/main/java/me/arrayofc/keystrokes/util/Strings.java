package me.arrayofc.keystrokes.util;

import net.minecraft.client.settings.KeyBinding;

/**
 * Utility class for strings.
 */
public class Strings {

    /**
     * Takes a string and turns it into a sentence with every word
     * starting with a capital letter.
     */
    public static String makeSentence(String str) {
        char[] chars = str.toCharArray();

        for (int i = 0; i < str.length(); ++i) {
            if (i == 0 && chars[i] != ' ' || chars[i] != ' ' && chars[i - 1] == ' ') {
                if (chars[i] >= 'a' && chars[i] <= 'z') {
                    chars[i] = (char) (chars[i] - 97 + 65);
                }
            } else if (chars[i] >= 'A' && chars[i] <= 'Z') {
                chars[i] = (char) (chars[i] + 97 - 65);
            }
        }

        return new String(chars);
    }

    /**
     * Returns the key name of a keybinding, made beautiful.
     *
     * @param keyBinding Keybinding to get name for
     * @return The beautified name of this key binding
     */
    public static String getKeyName(KeyBinding keyBinding) {
        String s = keyBinding.getTranslationKey().substring("key.keyboard.".length());
        return makeSentence(s.replace(".", " "));
    }
}