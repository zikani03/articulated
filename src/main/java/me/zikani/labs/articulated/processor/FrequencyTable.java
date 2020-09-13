package me.zikani.labs.articulated.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Frequency table for words - does not process spaces, single letter words and other special characters
 *
 */
public class FrequencyTable {
    private final Map<String, Integer> m = new HashMap<>();

    public void increment(String word) {
        String key = word.trim().strip();
        key = key.replaceAll("\\??\\.+\\\"?", "");
        if (key.isBlank() || key.isEmpty()) return;

        m.put(key, frequencyOf(key) + 1);
    }

    public int frequencyOf(String key) {
       return m.getOrDefault(key, 0);
    }

    public Map<String, Integer> getTable() {
        return Collections.unmodifiableMap(m);
    }
}
