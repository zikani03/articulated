package me.zikani.labs.articulated.dao;

import me.zikani.labs.articulated.model.Article;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindBeanList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.List;

@RegisterBeanMapper(Article.class)
public interface ArticleDAO {

    @SqlQuery("SELECT id, url, title, author, publishedOn, body, readingTime, created FROM articles WHERE id = :id")
    Article get(@Bind String id);

    @SqlQuery("SELECT id, url, title, author, publishedOn, body, readingTime, created FROM articles")
    List<Article> fetchAll();

    @SqlUpdate("CREATE VIRTUAL TABLE IF NOT EXISTS article_fts USING fts5(articleId UNINDEXED, body);")
    void createFtsTableIfNotExists();

    @SqlUpdate("INSERT INTO article_fts(articleId, body) SELECT id, body FROM articles;")
    void populateFts();

    @SqlUpdate("INSERT INTO article_fts(articleId, body) VALUES (:id, :body);")
    void indexArticleInFts(@BindBean Article article);

    @Transaction
    default void saveAndIndex(Article article) {
        save(article);
        indexArticleInFts(article);
    }

    @SqlQuery("""
    SELECT A.id, A.url, A.title, A.author, A.publishedOn, A.body, A.readingTime, A.created 
     FROM article_fts
     INNER JOIN articles A ON A.id = article_fts.articleId
     WHERE article_fts MATCH :query 
     ORDER BY rank""")
    List<Article> searchArticles(@Bind("query") String query);

    @SqlUpdate("""
    INSERT OR REPLACE INTO articles(id,  url,  title,  author,  publishedOn,  body,  readingTime,  created)
     VALUES(:id, :url, :title, :author, :publishedOn, :body, :readingTime, :created)
    """)
    void save(@BindBean Article article);

    @SqlBatch("""
    INSERT OR REPLACE INTO articles(id,  url,  title,  author,  publishedOn,  body,  readingTime,  created)
     VALUES(:id, :url, :title, :author, :publishedOn, :body, :readingTime, :created)
    """)
    void saveAll(@BindBean List<Article> articles);

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
