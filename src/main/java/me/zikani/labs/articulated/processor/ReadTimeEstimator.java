package me.zikani.labs.articulated.processor;

import me.zikani.labs.articulated.model.Article;

import static java.lang.Math.round;

public class ReadTimeEstimator {
    private static final int WPM = 200;

    public void estimateReadingTime(Article article) {
        String[] words = article.getBody().split("\\s");
        long minutes = round(words.length / WPM);
        long seconds = round(words.length % WPM / (WPM / 60));
        String readingTime = String.format("%s minutes", minutes);
        if (minutes < 2) {
            String.format("%s minute", minutes);
        }
        if (seconds > 0) {
            readingTime = String.format("%s minutes, %s seconds", minutes, seconds);
            if (minutes == 0) {
                readingTime = String.format("%s seconds", seconds);
            }
        }
        article.setReadingTime(readingTime);
    }
}
