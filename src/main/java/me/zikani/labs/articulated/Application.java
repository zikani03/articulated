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
package me.zikani.labs.articulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.WordFrequencyDAO;
import me.zikani.labs.articulated.fetch.ArticleFetcherFactory;
import me.zikani.labs.articulated.nlp.NeriaNamedEntityRecognitionService;
import me.zikani.labs.articulated.web.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.nio.file.Paths;

import static spark.Spark.*;

public class Application {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final int SLEEP_DURATION = 10_000;

    public static void main(String... args) {
        objectMapper.findAndRegisterModules();

        String databasePath = Paths.get("./articulated.db").toAbsolutePath().toString();
        final Jdbi jdbi = Jdbi.create(String.format("jdbc:sqlite:%s", databasePath));
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new SQLitePlugin());

        String neriaURL = System.getProperty("neria.url", "https://neria-fly.fly.dev");
        final WordFrequencyDAO wordFrequencyDAO = jdbi.onDemand(WordFrequencyDAO.class);
        final ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);

        articleDAO.createTable();
        articleDAO.createFtsTableIfNotExists();
        wordFrequencyDAO.createTable();

        ipAddress(System.getProperty("server.host", "localhost"));
        port(Integer.parseInt(System.getProperty("server.port", "4567")));

        staticFileLocation("public");

        Spark.get("/articles", new ArticlesListRoute(objectMapper, articleDAO));
        Spark.get("/articles/search", new ArticleSearchRoute(objectMapper, articleDAO));
        Spark.get("/articles/label", new ArticleLabelRoute(objectMapper, articleDAO));
        Spark.get("/articles/amounts", new ArticleAmountsRoute(objectMapper, articleDAO));
        Spark.get("/articles/download/from", new ArticleFetcherRoute(objectMapper, new ArticleFetcherFactory(), articleDAO));
        Spark.post("/articles/download/:site/:category", new ArticleDownloadRoute(objectMapper, articleDAO, SLEEP_DURATION));
        Spark.get("/articles/entities/:id", new ArticleNamedEntitiesResource(objectMapper, articleDAO, new NeriaNamedEntityRecognitionService(neriaURL, objectMapper)));
        Spark.get("/articulated.db", new DatabaseDownloadRoute(databasePath));
    }
}
