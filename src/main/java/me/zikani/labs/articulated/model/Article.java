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
package me.zikani.labs.articulated.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static me.zikani.labs.articulated.model.Amount.KWACHA_REGEX_2;

public class Article implements Comparable<Article> {
    @JsonProperty private String id;
    @JsonProperty private String url;
    @JsonProperty private String title;
    @JsonProperty private String author;
    @JsonProperty private LocalDate publishedOn;
    @JsonProperty private String body;
    @JsonProperty private String readingTime;
    @JsonProperty private Timestamp created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDate getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(LocalDate publishedOn) {
        this.publishedOn = publishedOn;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(String readingTime) {
        this.readingTime = readingTime;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publishedOn=" + publishedOn +
                ", readingTime='" + readingTime + '\'' +
                ", created=" + created +
                '}';
    }

    /**
     * Implements compareTo using comparison of the publish date if available
     * otherwise using a simple lexicographic comparison on the article title
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Article o) {
        if (this.publishedOn == null || o.publishedOn == null) {
            return this.getTitle().compareTo(o.getTitle());
        }
        return this.publishedOn.compareTo(o.publishedOn);
    }

    @JsonProperty
    public List<Amount> getMentionedAmounts() {
        List<Amount> amounts = new ArrayList<>();

        Matcher m = KWACHA_REGEX_2.matcher(this.body.toLowerCase());

        while(m.find()) {
            amounts.add(new Amount("MWK", Double.parseDouble(m.group("amount")), m.group("denomination")));
        }

        return amounts;
    }
}
