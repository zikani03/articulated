package me.zikani.labs.articulated.processor;

import me.zikani.labs.articulated.model.Article;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Counts the frequency of the words in an Article
 *
 */
public class WordFrequencyCounter {
    public Map<String, Integer> count(Article article) {
        var frequencyTable = new FrequencyTable();
        countInto(article, frequencyTable);
        return frequencyTable.getTable();
    }

    public Map<String, Integer> countInto(Article article, FrequencyTable frequencyTable) {
        Objects.requireNonNull(article, "article cannot be null");
        String body = article.getBody();
        Objects.requireNonNull(body, "article.body cannot be null");
        Objects.requireNonNull(frequencyTable, "frequencyTable cannot be null");
        Arrays.stream(body.toLowerCase().split("\\b"))
            .forEach(frequencyTable::increment);

        return frequencyTable.getTable();
    }

    public Map<String, Integer> countForAll(Set<Article> articles) {
        FrequencyTable frequencyTable = new FrequencyTable();
        articles.forEach(article -> {
            countInto(article, frequencyTable);
        });

        return frequencyTable.getTable();
    }

}
