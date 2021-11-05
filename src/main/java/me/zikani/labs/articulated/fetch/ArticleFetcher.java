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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class ArticleFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleFetcher.class);
    HttpClient client;
    final ArticleParser articleParser;
    final ConcurrentSkipListSet<Article> fetchedArticles;

    public ArticleFetcher(ArticleParser articleParser) {
        this.fetchedArticles = new ConcurrentSkipListSet<>();
        this.articleParser = articleParser;
    }

    public ConcurrentSkipListSet<Article> getFetchedArticles() {
        return fetchedArticles;
    }

    public CompletableFuture<Stream<Article>> fetchFrom(String category, int page) throws InterruptedException, ExecutionException {
        if (client == null) {
            client = HttpClient.newBuilder()
                    // TODO: use when loom lands : .executor(Executors.newVirtualThreadExecutor())
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }
        var stringBodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        var url = articleParser.getCategoryPageUrl(category, page);

        var req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        LOGGER.info("Fetching articles from url={}", url);
        return client.sendAsync(req, stringBodyHandler)
            .whenComplete((stringHttpResponse, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Got an exception while requesting page: {}", stringHttpResponse.body(), throwable);
                }
            })
            .thenApply(response -> articleParser.followArticleLinks(response.body()))
            .thenApply(articles -> articles.parallelStream()
                .map(article -> {
                    LoggerFactory.getLogger(getClass()).info("Fetching article: {}", article.getUrl());
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(article.getUrl()))
                        .build();

                    return client.sendAsync(request, stringBodyHandler)
                        .thenApply(response -> articleParser.parseArticle(response.body(), article))
                        .whenComplete((fetchedArticle, exception) -> {
                            if (exception != null) {
                                LOGGER.error("Failed to read article.", exception);
                                return;
                            }
                            fetchedArticles.add(fetchedArticle);
                        })
                        .toCompletableFuture();

                })
                // The caller must do something with the result otherwise the completable future wont complete!
                .map(CompletableFuture::join));
    }

    public void clearFetchedArticles() {
        fetchedArticles.clear();
    }
}
