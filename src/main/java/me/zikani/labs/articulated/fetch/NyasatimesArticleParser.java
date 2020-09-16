package me.zikani.labs.articulated.fetch;

import me.zikani.labs.articulated.model.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.StringJoiner;

/**
 * Parses articles from <a href="https://nyasatimes.com">Nyasa Times</a>
 * The version of this class is based on the last time the implementation was
 * working with the structure of Nyasatimes HTML pages. Which may (and has!)
 * change over time.
 *
 * @version 2020-09-16
 */
public class NyasatimesArticleParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NyasatimesArticleParser.class);

    /**
     * Parses string containing html of an article on
     * <a href="https://nyasatimes.com">Nyasa Times</a>
     * and maps it to an {@link Article} instance
     * @param body
     * @param article
     * @return
     */
    public Article parseArticle(String body, final Article article) {
        try {
            Element document = Jsoup.parse(body).getElementById("content");
            /**
             * We are dealing with a structure like this for the HTML of the Article
             *
             * <h1 class="entry-title">Water, food security project formed to benefit 270mln people in Malawi, Kenya, Ghana</h1>
             *
             * <div class="entry-meta">
             *   <span class="glyphicon glyphicon-calendar"></span> August 15, 2018
             *   <span class="glyphicon glyphicon-user"> </span> Duncan Mlanjira - Nyasa Times
             *   <a href="https://www.nyasatimes.com/water-food-security-project-formed-to-benefit-270mln-people-in-malawi-kenya-ghana/#respond"> <span class="glyphicon glyphicon-comment"> </span> Be the first to comment
             * </div>
             *
             * <div class="entry-content">
             *    <div rel="auto">Some text here</div>
             * </div>
             */
            article.setTitle(
                // h1.entry-title
                document.selectFirst("h1.nyasa-title").text()
            );
            StringJoiner sj = new StringJoiner("\n");
            document.select("div.nyasa-content")
                .eachText()
                .forEach(text -> sj.add(text));

            article.setBody(sj.toString());

            Element meta= document.selectFirst("div.entry-meta");

            var formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            var date = meta.textNodes().get(1).text();
            article.setPublishedOn(LocalDate.parse(date.trim(), formatter));

            var author = meta.textNodes().get(2).text();
            article.setAuthor(author.strip());
        } catch (DateTimeParseException ex) {
            LOGGER.error("Failed to parse article date", ex);
        } catch (Exception ex) {
            LOGGER.error("Failed to parse article body", ex);
        }
        return article;
    }
}
