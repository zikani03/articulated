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
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

public class ArticlesListByDateRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;

    public ArticlesListByDateRoute(ObjectMapper objectMapper, ArticleDAO articleDAO) {
        super(objectMapper);
        this.articleDAO = articleDAO;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type(APPLICATION_JSON.asString());
        String dateParam = request.params("date");
        try {
            var date = LocalDate.parse(dateParam);
            return objectMapper.writeValueAsString(singletonMap("articles", articleDAO.findAllByDate(date)));
        } catch (DateTimeParseException e) {
            return badRequest(response, singletonMap("message", "invalid date param"));
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("failed to proces request got error", e);
            return internalServerError(response, singletonMap("message", "server error processing request"));
        }
    }
}
