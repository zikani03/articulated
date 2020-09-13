package me.zikani.labs.articulated.dao;

import me.zikani.labs.articulated.model.Article;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterBeanMapper(Article.class)
public interface ArticleDAO {

    @SqlQuery("SELECT id, url, title, author, publishedOn, body, readingTime, created FROM articles WHERE id = :id")
    Article get(@Bind String id);

    @SqlUpdate("INSERT OR REPLACE INTO articles(id,  url,  title,  author,  publishedOn,  body,  readingTime,  created) " +
              "              VALUES(:id, :url, :title, :author, :publishedOn, :body, :readingTime, :created)")
    void save(@BindBean Article article);

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
    void createTable();
}
