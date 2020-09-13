package me.zikani.labs.articulated;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class ArticleDownloadMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final int SLEEP_DURATION = 10_000;

    public static void main(String... args) throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        final Jdbi jdbi = Jdbi.create(String.format("jdbc:sqlite:./nyasatimes-%s.db", today));
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new SQLitePlugin());


        final WordFrequencyDAO wordFrequencyDAO = jdbi.onDemand(WordFrequencyDAO.class);
        final ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);

        articleDAO.createTable();
        wordFrequencyDAO.createTable();

        // For all the articles fetched
        // final WordFrequencyCount wfc = new WordFrequencyCount();
        // final NamedEntityExtractor nme = new NamedEntityExtractor();
        ArticleFetcher articleFetcher = new ArticleFetcher();
        var pages = IntStream.rangeClosed(1, 5);
        final WordFrequencyCounter wfc = new WordFrequencyCounter();

        // There are about 303 pages: range(1, 303)
        for(int page: pages.toArray()) {
            try {
                final ReadTimeEstimator estimator = new ReadTimeEstimator();

                articleFetcher.fetchFrom("business", page)
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

            // Count words from the articles we have
            wfc.countForAll(articleSet).forEach((word, frequency) -> {
                LOGGER.info("Found word {}={}", word, frequency);
                wordFrequencyDAO.insert(word, frequency);
            });

            articleFetcher.clearFetchedArticles();

            LOGGER.info("Sleeping for {} seconds before processing page {}", SLEEP_DURATION / 1000, page);
            Thread.sleep(SLEEP_DURATION);
        }
    }
}
