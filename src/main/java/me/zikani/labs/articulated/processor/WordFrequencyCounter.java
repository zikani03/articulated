/**
 * MIT License
 *
 * Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
