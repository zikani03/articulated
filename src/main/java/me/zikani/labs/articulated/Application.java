package me.zikani.labs.articulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.WordFrequencyDAO;
import me.zikani.labs.articulated.fetch.ArticleFetcher;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.processor.ReadTimeEstimator;
import me.zikani.labs.articulated.processor.WordFrequencyCounter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

public class Application {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final int SLEEP_DURATION = 10_000;

    public static void main(String... args) {
        // String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        final Jdbi jdbi = Jdbi.create("jdbc:sqlite:./nyasatimes.db");
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new SQLitePlugin());

        final WordFrequencyDAO wordFrequencyDAO = jdbi.onDemand(WordFrequencyDAO.class);
        final ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);

        articleDAO.createTable();
        wordFrequencyDAO.createTable();

        Spark.get("/articles", (request, response) -> {
            response.type(APPLICATION_JSON.asString());
            return objectMapper.writeValueAsString(singletonMap("articles", articleDAO.fetchAll()));
        });

        Spark.post("/articles/download/:category", (request, response) -> {
            // For all the articles fetched
            String categoryName = request.params("category"); // business
            // final WordFrequencyCount wfc = new WordFrequencyCount();
            // final NamedEntityExtractor nme = new NamedEntityExtractor();
            ArticleFetcher articleFetcher = new ArticleFetcher();
            var pages = IntStream.rangeClosed(1, 5);
            final WordFrequencyCounter wfc = new WordFrequencyCounter();

            // There are about 303 pages: range(1, 303)
            for(int page: pages.toArray()) {
                try {
                    final ReadTimeEstimator estimator = new ReadTimeEstimator();

                    articleFetcher.fetchFrom(categoryName, page)
                            .thenAccept(articleStream -> {
                                articleStream.parallel()
                                        .forEach(article -> {
                                            estimator.estimateReadingTime(article);
                                            //nme.findNames(article);
                                            LOGGER.info("Fetched article {}", article);
                                        });
                            })
                            .join();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Failed to fetch pages", e);
                }

                Set<Article> articleSet = articleFetcher.getFetchedArticles();

                LOGGER.info("Saving {} articles to database", articleSet.size());
                articleSet.forEach(articleDAO::save);

//                // Count words from the articles we have
//                wfc.countForAll(articleSet).forEach((word, frequency) -> {
//                    LOGGER.info("Found word {}={}", word, frequency);
//                    wordFrequencyDAO.insert(word, frequency);
//                });

                articleFetcher.clearFetchedArticles();

                LOGGER.info("Sleeping for {} seconds before processing page {}", SLEEP_DURATION / 1000, page);
                Thread.sleep(SLEEP_DURATION);
            }

            return "Downloaded";
        });
    }
}
