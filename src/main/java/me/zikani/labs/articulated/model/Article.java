package me.zikani.labs.articulated.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public LocalDateTime getCreated() {
        return created.toLocalDateTime();
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
}
