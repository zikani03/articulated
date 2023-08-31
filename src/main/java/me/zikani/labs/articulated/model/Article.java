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
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            amounts.add(new Amount("MWK", new BigDecimal(m.group("amount")), m.group("denomination")));
        }

        return amounts;
    }

    public int countOccurencesOf(String keyword) {
        final var lowerBody = this.body.toLowerCase();
        var keywordCapture = String.format("(?<keyword>\s?(%s)\s?)", keyword.toLowerCase());
        Pattern keywordPattern = Pattern.compile(keywordCapture);
        Matcher m = keywordPattern.matcher(this.body.toLowerCase());
        int occurences = 0;
        while(m.find()) {
            occurences++;
        }
        return occurences;
    }

    public static ArticleFeature0 makeFeature(Article article) {
        ArticleFeature0 articleFeatureData = new ArticleFeature0();
        articleFeatureData.features = new HashMap<>();

        BigDecimal minAmount = BigDecimal.ZERO, maxAmount = BigDecimal.ZERO;
        int hundreds = 0, thousands = 0, millions = 0, billions = 0, trillions = 0, overTrillions = 0;
        for (Amount amount: article.getMentionedAmounts()) {
            minAmount = minAmount.min(amount.getAmount());
            maxAmount = maxAmount.max(amount.getAmount());

            if (amount.getAmount().longValue()  >= 100 && amount.getAmount().longValue() < 1e3) {
                hundreds++;
            } else if (amount.getAmount().longValue()  >= 1e3 && amount.getAmount().longValue() < 1e6) {
                thousands++;
            } else if (amount.getAmount().longValue()  >= 1e6 && amount.getAmount().longValue() < 1e9) {
                millions++;
            } else if (amount.getAmount().longValue()  >= 1e9 && amount.getAmount().longValue() < 1e12) {
                billions++;
            } else if (amount.getAmount().longValue()  >= 1e12 && amount.getAmount().longValue() < 1e15) {
                trillions++;
            } else if (amount.getAmount().longValue() > 1e15) {
                overTrillions++;
            }
        }

        articleFeatureData.features.put("article_id", article.getId());
        articleFeatureData.features.put("article_title", article.getTitle());
        articleFeatureData.features.put("min_amount_mwk", minAmount);
        articleFeatureData.features.put("max_amount_mwk", maxAmount);
        articleFeatureData.features.put("num_hundreds_values_mwk", hundreds);
        articleFeatureData.features.put("num_thousands_values_mwk", thousands);
        articleFeatureData.features.put("num_millions_values_mwk", millions);
        articleFeatureData.features.put("num_billions_values_mwk", billions);
        articleFeatureData.features.put("num_trillions_values_mwk", trillions);
        articleFeatureData.features.put("num_greater_than_trillions_values_mwk", overTrillions);
        articleFeatureData.features.put("timestamp", article.getPublishedOn().toEpochSecond(LocalTime.now(), ZoneOffset.UTC));

        articleFeatureData.countAndSet("budget", "budget", article);
        articleFeatureData.countAndSet("revenue", "revenue|revenues|profits|sales", article);
        articleFeatureData.countAndSet("borrowed", article);
        articleFeatureData.countAndSet("credit", article);
        articleFeatureData.countAndSet("funding", "fund|funding", article);
        articleFeatureData.countAndSet("donations", "donates|donation", article);
        articleFeatureData.countAndSet("fundraiser", "fundraiser|fundraising", article);
        articleFeatureData.countAndSet("loan", "loan|loans", article);
        articleFeatureData.countAndSet("corruption", article);
        articleFeatureData.countAndSet("bribes", article);
        articleFeatureData.countAndSet("mwk", article);
        articleFeatureData.countAndSet("usd", article);
        articleFeatureData.countAndSet("stolen", article);
        articleFeatureData.countAndSet("win", "wins|winner|winnings", article);
        articleFeatureData.countAndSet("disbursed", article);
        articleFeatureData.countAndSet("sponsorship", "sponsor|sponsors|sponsorship", article);

        return articleFeatureData;
    }

    @Data
    public static class ArticleFeature0 {
        private Map<String, Object> features;

        private void countAndSet(String keywordKey, Article article) {
            this.features.put(keywordKey, article.countOccurencesOf(keywordKey));
        }
        private void countAndSet(String keywordKey, String keywordValue, Article article) {
            this.features.put(keywordKey, article.countOccurencesOf(keywordValue));
        }
    }

}
