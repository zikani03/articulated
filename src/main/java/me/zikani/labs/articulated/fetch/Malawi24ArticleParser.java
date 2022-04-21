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
