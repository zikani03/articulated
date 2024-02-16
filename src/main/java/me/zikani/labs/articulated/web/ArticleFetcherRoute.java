/**
 * MIT License
 * <p>
 * Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.javalin.http.Context;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.fetch.ArticleFetcherFactory;
import me.zikani.labs.articulated.model.Article;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static me.zikani.labs.articulated.Utils.sha1;

/**
 * This route handles scraping and indexing for one article only.
 * It supports bot POST an GET requests
 */
public class ArticleFetcherRoute extends AbstractBaseRoute {
    private final ArticleFetcherFactory factory;
    private final ArticleDAO articleDAO;

    public ArticleFetcherRoute(ObjectMapper objectMapper, ArticleFetcherFactory articleFetcher, ArticleDAO articleDAO) {
        super(objectMapper);
        this.factory = articleFetcher;
        this.articleDAO = articleDAO;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        String articleUrl = context.queryParam("url");
        Article article = Optional.ofNullable(articleDAO.get(sha1(articleUrl)))
                .orElseGet(() -> {
                    try {
                        Article newArticle = factory.getFetcherForURL(articleUrl).fetchSingleArticle(articleUrl).get();
                        articleDAO.saveAndIndex(newArticle);
                        return newArticle;
                    } catch (Exception e) {
                        LoggerFactory.getLogger(ArticleFetcherRoute.class).error("Failed to fetch article", e);
                        return null;
                    }
                });

        context.json(article);
    }
}
