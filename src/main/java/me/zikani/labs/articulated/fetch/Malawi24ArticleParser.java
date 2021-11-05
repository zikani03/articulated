package me.zikani.labs.articulated.fetch;

import me.zikani.labs.articulated.model.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static me.zikani.labs.articulated.Utils.sha1;


public class Malawi24ArticleParser implements ArticleParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Malawi24ArticleParser.class);

    /**
     * Parses string containing html of an article and maps it to an {@link Article} instance
     * @param body
     * @param article
     * @return
     */
    public Article parseArticle(String body, final Article article) {
        try {
            Element document = Jsoup.parse(body).getElementById("main-content");
            article.setTitle(
                // h1.entry-title
                document.selectFirst("h1.entry-title").text()
            );
            StringJoiner sj = new StringJoiner("\n");
            document.select("div.entry-content p")
                .eachText()
                .forEach(text -> sj.add(text));

            article.setBody(sj.toString());

            Element meta= document.selectFirst("div.entry-meta");

            var formatter = DateTimeFormatter.ofPattern("MMM dd, u");
            var author = meta.selectFirst(".entry-meta-author").selectFirst("a").text();
            article.setAuthor(author.strip());

            var date = meta.selectFirst(".entry-meta-date").selectFirst("a").text();
            article.setPublishedOn(LocalDate.parse(date.trim(), formatter));

        } catch (DateTimeParseException ex) {
            LOGGER.error("Failed to parse article date", ex);
        } catch (Exception ex) {
            LOGGER.error("Failed to parse article body", ex);
        }
        return article;
    }

    public List<Article> followArticleLinks(String categoryPageBody) {
        Document document = Jsoup.parse(categoryPageBody);
        return document.select(".entry-title.posts-list-title > a")
                .stream()
                .map(anchor -> {
                    String url = anchor.attr("href");
                    // LoggerFactory.getLogger(getClass()).info("Found anchor: {}", url);
                    var a = new Article();
                    a.setUrl(url);
                    a.setId(sha1(url));
                    a.setCreated(Timestamp.valueOf(LocalDateTime.now()));
                    return a;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getCategoryPageUrl(String category, int page) {
        var url = String.format("https://www.malawi24.com/category/%s/page/%s/", category, page);
        if (page == 1) {
            url = String.format("https://www.malawi24.com/category/%s/", category);
        }
        return url;
    }
}
