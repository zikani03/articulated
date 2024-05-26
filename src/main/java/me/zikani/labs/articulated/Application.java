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
package me.zikani.labs.articulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import me.zikani.labs.articulated.articleworker.NamedEntityWorker;
import me.zikani.labs.articulated.dao.*;
import me.zikani.labs.articulated.fetch.ArticleFetcherFactory;
import me.zikani.labs.articulated.greypot.GreypotHttpClient;
import me.zikani.labs.articulated.kafka.KafkaArticleConsumer;
import me.zikani.labs.articulated.kafka.KafkaArticlePublisher;
import me.zikani.labs.articulated.model.EntitySub;
import me.zikani.labs.articulated.nlp.NeriaNamedEntityExtractor;
import me.zikani.labs.articulated.web.*;
import org.eclipse.jetty.websocket.api.Session;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisFactory;
import redis.clients.jedis.JedisPool;

import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

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
        final ArticleRatingDAO articleRatingDAO = jdbi.onDemand(ArticleRatingDAO.class);
        final EntityDAO entityDAO = jdbi.onDemand(EntityDAO.class);
        final MigrationsDAO migrations = jdbi.onDemand(MigrationsDAO.class);

        migrations.runAll();

        articleDAO.createFtsTableIfNotExists();
        wordFrequencyDAO.createTable();

        var jedisPool = new JedisPool("localhost", 6379);

        var worker = new NamedEntityWorker(
                articleDAO,
                entityDAO,
                new NeriaNamedEntityExtractor(neriaURL, objectMapper),
                jedisPool,
                objectMapper
        );

        //Executors.newVirtualThreadPerTaskExecutor().execute(worker);
        var app = Javalin.create(config -> {
            config.staticFiles.add("public");
        });

        app.get("/articles", new ArticlesListRoute(objectMapper, articleDAO));
        app.get("/articles/published-on/{date}", new ArticlesListByDateRoute(objectMapper, articleDAO));
        app.get("/articles/label", new ArticleLabelRoute(objectMapper, articleDAO));
        app.get("/articles/random", new ArticleGetRandomRoute(objectMapper, articleDAO));
        app.get("/articles/search", new ArticleSearchRoute(objectMapper, articleDAO));
        app.get("/articles/amounts", new ArticleAmountsRoute(objectMapper, articleDAO));
        app.get("/articles/amounts/feature0", new ArticleFeature0Route(objectMapper, articleDAO));
        app.get("/articles/download/from", new ArticleFetcherRoute(objectMapper, new ArticleFetcherFactory(), articleDAO));
        app.post("/articles/download/{site}/{category}", new ArticleDownloadRoute(objectMapper, articleDAO, SLEEP_DURATION));
        app.get("/articles/entities/{id}", new ArticleNamedEntitiesResource(objectMapper, articleDAO, new NeriaNamedEntityExtractor(neriaURL, objectMapper)));
        app.get("/articles/word-cloud/{id}", new ArticleWordCloudRoute(objectMapper, articleDAO, Paths.get("./bin/")));
        app.get("/articulated.db", new DatabaseDownloadRoute(databasePath));
        app.get("/articles/{id}/pdf", new ArticlesPDFRoute(objectMapper, articleDAO, new GreypotHttpClient(greypotURL, objectMapper)));
        app.post("/natty", new NattyRoute());
        app.sse("/sse", new ServerSentEventsRoute(new ArrayDeque<>()));

        Logger wslogger = LoggerFactory.getLogger("websockets");

        app.ws("/ws/entities", ctx -> {
            ctx.onConnect(handler -> {
                wslogger.info("got a new connection from a websocket client");
            });

            ctx.onMessage(handler -> {
                var sub = handler.messageAsClass(EntitySub.class);
                wslogger.info("got message from client {}", sub.name());
                handler.send("No articles found for this topic");
                wslogger.info("sent message to client");
            });

            ctx.onClose(handler -> {
                wslogger.info("closed websocket connection");
            });
        });

        var enableKafkaPublisher = Boolean.getBoolean("kafka.enabled");
        if (enableKafkaPublisher) {
            Properties props = new Properties();
            props.setProperty("bootstrap.servers", System.getProperty("kafka.servers", "localhost:19092"));
            props.put("group.id", "articulated-0");
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");


            Executors.newVirtualThreadPerTaskExecutor().execute(new KafkaArticleConsumer("article_ner_finder", props));


            final KafkaArticlePublisher kafkaPublisher = new KafkaArticlePublisher("article_ner_finder", props);
            app.post("/articles/publish-to-kafka", new ArticleKafkaPublisherRoute(objectMapper, articleDAO, kafkaPublisher));
        }

        var host = System.getProperty("server.host", "localhost");
        var port = Integer.parseInt(System.getProperty("server.port", "4567"));

        app.start(host, port);

    }
}
