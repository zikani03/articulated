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
import me.zikani.labs.articulated.fetch.ArticleFetcher;
import me.zikani.labs.articulated.fetch.ArticleParser;
import me.zikani.labs.articulated.fetch.Malawi24ArticleParser;
import me.zikani.labs.articulated.fetch.NyasatimesArticleParser;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.processor.ReadTimeEstimator;
import me.zikani.labs.articulated.processor.WordFrequencyCounter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static java.util.Collections.singletonMap;

public class ArticleDownloadRoute extends AbstractBaseRoute {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleDownloadRoute.class);
    private static int SLEEP_DURATION = 10_000;
    private final ArticleDAO articleDAO;
    private final WordFrequencyCounter frequencyCounter;

    public ArticleDownloadRoute(ObjectMapper objectMapper, ArticleDAO articleDAO, int sleepDurationSeconds) {
        super(objectMapper);
        this.articleDAO = articleDAO;
        this.frequencyCounter = new WordFrequencyCounter();
        SLEEP_DURATION = sleepDurationSeconds;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {


        String siteName = context.pathParam("site").toLowerCase();
        String categoryName = context.pathParam("category");
        int fromPage = Integer.parseInt(context.queryParam("from"));
        int toPage = Integer.parseInt(context.queryParam("to"));
        // final WordFrequencyCount wfc = new WordFrequencyCount();
        // final NamedEntityExtractor nme = new NamedEntityExtractor();
        ArticleParser articleParser = switch (siteName) {
            case "nyasatimes" -> new NyasatimesArticleParser();
            case "malawi24" -> new Malawi24ArticleParser();
            default -> new NyasatimesArticleParser();
        };
        ArticleFetcher articleFetcher = new ArticleFetcher(articleParser);

        var pages = IntStream.rangeClosed(fromPage, toPage);

        // There are about 303 pages: range(1, 303)
        for (int page : pages.toArray()) {
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
            articleSet.forEach(articleDAO::saveAndIndex);

//                // Count words from the articles we have
//                wfc.countForAll(articleSet).forEach((word, frequency) -> {
//                    LOGGER.info("Found word {}={}", word, frequency);
//                    wordFrequencyDAO.insert(word, frequency);
//                });

            articleFetcher.clearFetchedArticles();

            LOGGER.info("Sleeping for {} seconds before processing page {}", SLEEP_DURATION / 1000, page);
            Thread.sleep(SLEEP_DURATION);
        }

        context.json(singletonMap("message", "Download completed"));
    }
}
