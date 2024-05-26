package me.zikani.labs.articulated.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface ArticleRatingDAO {
    /*
     * Query to generate random ratings:
     *
     * INSERT INTO article_ratings(article_id, rating, username, created_at)
     * SELECT id, abs(random() % 5), 'mom', unixepoch() from articles;
     */
    @SqlUpdate("INSERT INTO article_ratings(article_id, rating, username, created_at) VALUES(:articleId, :rating, :username, :timestamp)")
    void addRating(@Bind("articleId") String articleId, @Bind("rating") int rating, @Bind("username") String username, @Bind("timestamp") long timestamp);
}
