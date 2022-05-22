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

/**
 * Parses articles from <a href="https://nyasatimes.com">Nyasa Times</a>
 * The version of this class is based on the last time the implementation was
 * working with the structure of Nyasatimes HTML pages. Which may (and has!)
 * change over time.
 *
 * @version 2020-09-16
 */
public class NyasatimesArticleParser implements ArticleParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NyasatimesArticleParser.class);

    @Override
    public boolean canParseFrom(String url) {
        return url.contains("nyasatimes.com");
    }
    /**
     * Parses string containing html of an article on
     * <a href="https://nyasatimes.com">Nyasa Times</a>
     * and maps it to an {@link Article} instance
     * @param body
     * @param article
     * @return
     */
    @Override
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

    /**
     * Given the HTML from a page like https://nyasatimes.com/category/business/page/2/
     * extracts all the links to the articles from that page into a list
     * of string uris/urls
     *
     * @param categoryPageBody
     * @return
     */
    @Override
    public List<Article> followArticleLinks(String categoryPageBody) {
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

    @Override
    public String getCategoryPageUrl(String category, int page) {
        var url = String.format("https://www.nyasatimes.com/category/%s/page/%s/", category, page);
        if (page == 1) {
            url = String.format("https://www.nyasatimes.com/category/%s/", category);
        }
        return url;
    }
}
