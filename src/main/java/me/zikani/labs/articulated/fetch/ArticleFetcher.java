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
package me.zikani.labs.articulated.fetch;

import me.zikani.labs.articulated.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static me.zikani.labs.articulated.Utils.sha1;

public class ArticleFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleFetcher.class);
    HttpClient client;
    final ArticleParser articleParser;
    final ConcurrentSkipListSet<Article> fetchedArticles;

    public ArticleFetcher(ArticleParser articleParser) {
        this.fetchedArticles = new ConcurrentSkipListSet<>();
        this.articleParser = articleParser;
    }

    private HttpClient getHttpClient() {
        if (client == null) {
            client = HttpClient.newBuilder()
                    // TODO: use when loom lands : .executor(Executors.newVirtualThreadExecutor())
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }
        return client;
    }
    public ConcurrentSkipListSet<Article> getFetchedArticles() {
        return fetchedArticles;
    }

    public CompletableFuture<Stream<Article>> fetchFrom(String category, int page) throws InterruptedException, ExecutionException {
        var stringBodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        var url = articleParser.getCategoryPageUrl(category, page);

        var req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        LOGGER.info("Fetching articles from url={}", url);
        return getHttpClient().sendAsync(req, stringBodyHandler)
            .whenComplete((stringHttpResponse, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Got an exception while requesting page: {}", stringHttpResponse.body(), throwable);
                }
            })
            .thenApply(response -> articleParser.followArticleLinks(response.body()))
            .thenApply(articles -> articles.parallelStream()
                .map(article ->
                    this.fetchSingleArticle(article.getUrl())
                        .whenComplete((fetchedArticle, exception) -> {
                            if (exception != null) {
                                LOGGER.error("Failed to read article.", exception);
                                return;
                            }
                            fetchedArticles.add(fetchedArticle);
                        })
                        .toCompletableFuture())
                // The caller must do something with the result otherwise the completable future wont complete!
                .map(CompletableFuture::join));
    }

    public void clearFetchedArticles() {
        fetchedArticles.clear();
    }

    public CompletableFuture<Article> fetchSingleArticle(String articleUrl) {
        Article article = new Article();
        article.setUrl(Objects.requireNonNull(articleUrl, "articleUrl"));
        article.setId(sha1(articleUrl));
        var stringBodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        LoggerFactory.getLogger(getClass()).info("Fetching article: {}", article.getUrl());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(article.getUrl()))
                .build();

        return getHttpClient().sendAsync(request, stringBodyHandler)
                .thenApply(response -> articleParser.parseArticle(response.body(), article))
                .whenComplete((fetchedArticle, exception) -> {
                    if (exception != null) {
                        LOGGER.error("Failed to read article.", exception);
                        return;
                    }
                })
                .toCompletableFuture();
    }
}
