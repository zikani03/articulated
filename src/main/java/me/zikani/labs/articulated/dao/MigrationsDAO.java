package me.zikani.labs.articulated.dao;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

public interface MigrationsDAO {
    @SqlUpdate("CREATE TABLE IF NOT EXISTS articles (" +
            "id varchar(256) not null PRIMARY KEY," +
            "url text," +
            "title text," +
            "author varchar(200)," +
            "publishedOn date," +
            "body text," +
            "readingTime varchar(100)," +
            "created timestamp" +
            ");")
    void v1__AddArticlesTable();

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS entities (
            id text primary key,
            entity_name text,
            entity_type text,
            num_occurrences integer,
            created timestamp
        );
    """)
    void v2__AddEntitiesTable();

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS article_entities (
            entity_id text, -- hash of the entity name using hash function of choice
            article_id text,
            primary key(entity_id, article_id)
        );
    """)
    void v3__AddArticleEntitiesTable();

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS article_ratings (
            article_id text not null,
            username text not null,
            rating integer not null,
            created_at timestamp not null,
            primary key(article_id, username)
        );
    """)
    void v4__AddArticleRatingsTable();

    @Transaction
    default void runAll() {
        v1__AddArticlesTable();
        v2__AddEntitiesTable();
        v3__AddArticleEntitiesTable();
        v4__AddArticleRatingsTable();
    }
}
