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
import io.javalin.http.HttpStatus;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.greypot.GreypotClient;
import me.zikani.labs.articulated.model.Article;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class ArticlesPDFRoute extends AbstractBaseRoute {
    private static final String htmlTemplate = """
            <div>
              <h1>{{ data.title }}<h1>
              <h5>{{ data.author }}</h5>
              <p>{{ data.body }}</p>
            </div>
            """;
    private final ArticleDAO articleDAO;
    private final GreypotClient greypotClient;


    public ArticlesPDFRoute(ObjectMapper objectMapper, ArticleDAO articleDAO, GreypotClient client) {
        super(objectMapper);
        this.articleDAO = articleDAO;
        this.greypotClient = client;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {

        // TODO: make get return optional
        Optional<Article> articleOptional = Optional.ofNullable(articleDAO.get(context.pathParam("id")));
        if (articleOptional.isEmpty()) {
            context.status(HttpStatus.NOT_FOUND);
            return;
        }
        var rpt = greypotClient.generatePDF("test", htmlTemplate, articleOptional.get());
        if (rpt.isPresent()) {
            try {
                context.contentType("application/pdf");
                context.outputStream().write(rpt.get().dataAsByteArray());
            } catch (IOException e) {

            }
        }
    }
}
