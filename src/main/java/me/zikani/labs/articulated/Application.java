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
import me.zikani.labs.articulated.articleworker.NamedEntityResolverWorker;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.EntityDAO;
import me.zikani.labs.articulated.dao.MigrationsDAO;
import me.zikani.labs.articulated.dao.WordFrequencyDAO;
import me.zikani.labs.articulated.fetch.ArticleFetcherFactory;
import me.zikani.labs.articulated.greypot.GreypotHttpClient;
import me.zikani.labs.articulated.kafka.KafkaArticlePublisher;
import me.zikani.labs.articulated.nlp.NeriaNamedEntityRecognitionService;
import me.zikani.labs.articulated.web.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;

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

        String neriaURL = System.getProperty("neria.url", "http://localhost:8080");
        String greypotURL = System.getProperty("greypot.url", "https://greypot-studio.fly.dev");

        final WordFrequencyDAO wordFrequencyDAO = jdbi.onDemand(WordFrequencyDAO.class);
        final ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);
        final EntityDAO entityDAO = jdbi.onDemand(EntityDAO.class);
        final MigrationsDAO migrations = jdbi.onDemand(MigrationsDAO.class);

        migrations.runAll();

        articleDAO.createFtsTableIfNotExists();
        wordFrequencyDAO.createTable();

        var worker = new NamedEntityResolverWorker(
                articleDAO, entityDAO,
                new NeriaNamedEntityRecognitionService(neriaURL, objectMapper), objectMapper
        );

        Executors.newVirtualThreadPerTaskExecutor().execute(worker);

        ipAddress(System.getProperty("server.host", "localhost"));
        port(Integer.parseInt(System.getProperty("server.port", "4567")));

        staticFileLocation("public");

        Spark.get("/articles", new ArticlesListRoute(objectMapper, articleDAO));
        Spark.get("/articles/published-on/:date", new ArticlesListByDateRoute(objectMapper, articleDAO));
        Spark.get("/articles/label", new ArticleLabelRoute(objectMapper, articleDAO));
        Spark.get("/articles/random", new ArticleGetRandomRoute(objectMapper, articleDAO));

        Spark.get("/articles/search", new ArticleSearchRoute(objectMapper, articleDAO));
        Spark.get("/articles/amounts", new ArticleAmountsRoute(objectMapper, articleDAO));
        Spark.get("/articles/amounts/feature0", new ArticleFeature0Route(objectMapper, articleDAO));
        Spark.get("/articles/download/from", new ArticleFetcherRoute(objectMapper, new ArticleFetcherFactory(), articleDAO));
        Spark.post("/articles/download/:site/:category", new ArticleDownloadRoute(objectMapper, articleDAO, SLEEP_DURATION));
        Spark.get("/articles/entities/:id", new ArticleNamedEntitiesResource(objectMapper, articleDAO, new NeriaNamedEntityRecognitionService(neriaURL, objectMapper)));
        Spark.get("/articulated.db", new DatabaseDownloadRoute(databasePath));
        Spark.get("/articles/:id/pdf", new ArticlesPDFRoute(objectMapper, articleDAO, new GreypotHttpClient(greypotURL, objectMapper)));
        Spark.post("/natty", new NattyRoute());

        Spark.get("/sse", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {

                String event = "event:hello\ndata:Hello\n\n";
                final OutputStream os = response.raw().getOutputStream();
                //Executors.newCachedThreadPool().execute(() -> {
                    OutputStreamWriter w = new OutputStreamWriter(os);
                    while(true) {
                        try {
                            w.write(event);
                            w.flush();
                            Thread.sleep(1_000);
                        } catch (Exception e) {
                            try {
                                os.close();
                            } catch (Exception inner) {}
                            throw new RuntimeException(e);
                        }
                    }
//                });

//                response.header("Connection", "keep-alive");
//                response.header("Content-Type", "text/event-stream");
//
//                return response.raw();
            }
        });

        var enableKafkaPublisher = Boolean.getBoolean("kafka.enabled");
        if (enableKafkaPublisher) {
            Properties props = new Properties();
            props.setProperty("bootstrap.servers", System.getProperty("kafka.servers", "localhost:9092"));
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

            final KafkaArticlePublisher kafkaPublisher = new KafkaArticlePublisher("article_ner_finder", props);
            Spark.post("/articles/publish-to-kafka", new ArticleKafkaPublisherRoute(objectMapper, articleDAO, kafkaPublisher));
        }
    }
}
