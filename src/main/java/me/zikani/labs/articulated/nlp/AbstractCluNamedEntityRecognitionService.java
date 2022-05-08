/*
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

package me.zikani.labs.articulated.nlp;

import me.zikani.labs.articulated.model.Article;
import org.clulab.processors.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCluNamedEntityRecognitionService implements NamedEntityRecognition {
    /**
     * Extracts named entities from the given article if any exist
     * @param article the article to extract data from
     * @return list of named entities found in the article body
     */
    protected  abstract Sentence[] extractSentences(final Article article);

    @Override
    public List<NamedEntity> extractNames(final Article article) {
        Objects.requireNonNull(article);
        if (article.getBody() == null || article.getBody().isEmpty()) {
            return Collections.emptyList();
        }
        List<NamedEntity> namedEntities = new ArrayList<>();
        Sentence[] sentences = extractSentences(article);
        for (int sentenceIndex = 0; sentenceIndex < sentences.length; sentenceIndex++) {
            Sentence sentence = sentences[sentenceIndex];
            if (sentence.entities().isDefined()) {
                for(String entityName: sentence.entities().get())
                    namedEntities.add(new NamedEntity(entityName,"GPE"));
            }
        }

        return namedEntities;
    }
}
