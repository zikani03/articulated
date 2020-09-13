package me.zikani.labs.articulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.WordFrequencyDAO;
import me.zikani.labs.articulated.fetch.ArticleFetcher;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.processor.ReadTimeEstimator;
import me.zikani.labs.articulated.processor.WordFrequencyCounter;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.EmbeddedJettyFactoryConstructor;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static java.util.Collections.singletonMap;

public class Application {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void createServerWithRequestLog(Logger logger) {
        EmbeddedJettyFactory factory = createEmbeddedJettyFactoryWithRequestLog();
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, factory);
    }

    private static EmbeddedJettyFactory createEmbeddedJettyFactoryWithRequestLog() {
        return new EmbeddedJettyFactoryConstructor(null).create();
    }

    public static void main(String... args) {
        createServerWithRequestLog(null);
        Spark.get("/articles", (request, response) -> {
            return objectMapper.writeValueAsString(singletonMap("artiles", "No articles here"));
        });
    }
}
