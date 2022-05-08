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

import com.typesafe.config.ConfigFactory;
import me.zikani.labs.articulated.model.Article;
import org.clulab.processors.Document;
import org.clulab.processors.Sentence;
import org.clulab.processors.clu.CluProcessor;
import org.clulab.sequences.LexiconNER;
import scala.Option;

import java.util.HashMap;
import java.util.Map;

public class ClulabProcessorNamedEntityRecognitionService extends AbstractCluNamedEntityRecognitionService {

    private CluProcessor cluProcessor;
     final static Option<LexiconNER> noLexiconNER = Option.empty();
     final static Option<String> noString = Option.empty();
     final static Map<String, Object> emptyMap = new HashMap();

    public ClulabProcessorNamedEntityRecognitionService() {
        this.cluProcessor = new CluProcessor(ConfigFactory.load("cluprocessor"), noLexiconNER, noString);
    }

    @Override
    protected Sentence[] extractSentences(Article article) {
        Document document = cluProcessor.annotate(article.getBody(), false);
        return document.sentences();
    }
}
