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
package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.processor.WordFrequencyCounter;
import spark.Request;
import spark.Response;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This endpoint outputs articles as plaintext file for labeling in a format that's
 * compatible with OpenCraftML project
 */
public class ArticleLabelRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;

    public ArticleLabelRoute(ObjectMapper objectMapper, ArticleDAO articleDAO) {
        super(objectMapper);
        this.articleDAO = articleDAO;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String query = request.queryParams("q");
        List<Article> articleList = articleDAO.searchArticles(query);
        WordFrequencyCounter wordFrequencyCounter = new WordFrequencyCounter();
        List<String> sb = new ArrayList<>();
        wordFrequencyCounter.countForAll(new HashSet<>(articleList))
                .forEach((word, freq) -> sb.add(String.format("%s\t=>\t%s", word, freq)));

        sb.sort(Comparator.naturalOrder());
        String res = sb.stream().collect(Collectors.joining("\n"));

        response.type("text/plain");

        return  res;
    }
}
