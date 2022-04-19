package me.zikani.labs.articulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.WordFrequencyDAO;
import me.zikani.labs.articulated.web.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.nio.file.Paths;

import static spark.Spark.ipAddress;
import static spark.Spark.port;

public class Application {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final int SLEEP_DURATION = 10_000;

    public static void main(String... args) {
        objectMapper.findAndRegisterModules();
        ipAddress(System.getProperty("server.host", "localhost"));
        port(Integer.parseInt(System.getProperty("server.port", "4567")));
        String databasePath = Paths.get("./articulated.db").toAbsolutePath().toString();
        final Jdbi jdbi = Jdbi.create(String.format("jdbc:sqlite:%s", databasePath));
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new SQLitePlugin());

        final WordFrequencyDAO wordFrequencyDAO = jdbi.onDemand(WordFrequencyDAO.class);
        final ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);

        articleDAO.createTable();
        articleDAO.createFtsTableIfNotExists();
        wordFrequencyDAO.createTable();

        Spark.get("/articles", new ArticlesListRoute(objectMapper, articleDAO));
        Spark.get("/articles/search", new ArticleSearchRoute(objectMapper, articleDAO));
        Spark.get("/articles/amounts", new ArticleAmountsRoute(objectMapper, articleDAO));
        Spark.post("/articles/download/:site/:category", new ArticleDownloadRoute(objectMapper, articleDAO, SLEEP_DURATION));
        Spark.get("/articulated.db", new DatabaseDownloadRoute(databasePath));
    }
}
