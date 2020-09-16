package me.zikani.labs.articulated.fetch;

import me.zikani.labs.articulated.model.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArticleFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleFetcher.class);
    HttpClient client;
    final NyasatimesArticleParser articleParser = new NyasatimesArticleParser();
    final ConcurrentSkipListSet<Article> fetchedArticles;

    public ArticleFetcher() {
        fetchedArticles = new ConcurrentSkipListSet<>();
    }

    public ConcurrentSkipListSet<Article> getFetchedArticles() {
        return fetchedArticles;
    }

    public CompletableFuture<Stream<Article>> fetchFrom(String category, int page) throws InterruptedException, ExecutionException {
        if (client == null) {
            client = HttpClient.newBuilder()
                    .executor(Executors.newVirtualThreadExecutor())
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
        }
        var stringBodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        var url = String.format("https://www.nyasatimes.com/category/%s/page/%s/", category, page);
        if (page == 1) {
            url = String.format("https://www.nyasatimes.com/category/%s/", category);
        }
        LOGGER.info("Fetching articles from url={}", url);
        var req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        return client.sendAsync(req, stringBodyHandler)
            .whenComplete((stringHttpResponse, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Got an exception while requesting page: {}", stringHttpResponse.body(), throwable);
                }
            })
            .thenApply(response -> followArticleLinks(response.body()))
            .thenApply(articles -> articles.parallelStream()
                .map(article -> {
                    LOGGER.info("Fetching article: " + article.getUrl());
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(article.getUrl()))
                        .build();
                    return client.sendAsync(request, stringBodyHandler)
                        .thenApply(response -> articleParser.parseArticle(response.body(), article))
                        .whenComplete((fetchedArticle, exception) -> {
                            if (exception != null) {
                                LOGGER.error("Failed to read article.", exception);
                            }
                            fetchedArticles.add(fetchedArticle);
                        })
                        .toCompletableFuture();
                })
                .map(CompletableFuture::join)
            );
    }

    /**
     * Given the HTML from a page like https://nyasatimes.com/category/business/page/2/
     * extracts all the links to the articles from that page into a list
     * of string uris/urls
     *
     * @param categoryPageBody
     * @return
     */
    private List<Article> followArticleLinks(String categoryPageBody) {
        Document document = Jsoup.parse(categoryPageBody);
        return document.select(".card-main-title > a")
            .stream()
            .map(anchor -> {
                var a = new Article();
                a.setUrl(anchor.attr("href"));
                a.setId(sha1(a.getUrl()));
                a.setCreated(Timestamp.valueOf(LocalDateTime.now()));
                return a;
            })
            .collect(Collectors.toList());
    }

    /**
     * Digest instance for computing hash of a text
     */
    static MessageDigest messageDigest;
    private static String sha1(String text) {
        if (messageDigest == null) {
            try {
                messageDigest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                // This shouldn't happen
                LOGGER.error("Failed to initialize MessageDigest instance for SHA-1", e);
                return text;
            }
        }
        return Hex.encode(messageDigest.digest(text.getBytes()));
    }

    public void clearFetchedArticles() {
        fetchedArticles.clear();
    }
}
