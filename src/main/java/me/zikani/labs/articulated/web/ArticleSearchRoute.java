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
import me.zikani.labs.articulated.model.Article;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Implements a simple full-text search for articles using SQLites Full-Text Search capabilities..
 *
 */
public class ArticleSearchRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;

    public ArticleSearchRoute(ObjectMapper objectMapper, ArticleDAO articleDAO) {
        super(objectMapper);
        this.articleDAO = articleDAO;
    }


    @Override
    public void handle(@NotNull Context context) throws Exception {
        String query = context.queryParam("q");
        if (query == null || query.isEmpty()) {
            context.json(emptyList());
            return;
        }
        List<Article> articleList = articleDAO.searchArticles(query);
        context.json(articleList);
    }
}
