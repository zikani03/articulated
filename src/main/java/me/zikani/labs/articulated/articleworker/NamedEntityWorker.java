package me.zikani.labs.articulated.articleworker;

import me.zikani.labs.articulated.Utils;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.EntityDAO;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.nlp.NamedEntity;
import me.zikani.labs.articulated.nlp.NamedEntityExtractor;

import java.util.Objects;

public class NamedEntityWorker implements Runnable {

    private final ArticleDAO articleDAO;
    private final EntityDAO entityDAO;
    private final NamedEntityExtractor namedEntityExtractor;


    public NamedEntityWorker(final ArticleDAO articleDAO,
                             final EntityDAO entityDAO,
                             final NamedEntityExtractor namedEntityExtractor) {
        this.articleDAO = Objects.requireNonNull(articleDAO);
        this.entityDAO = Objects.requireNonNull(entityDAO);
        this.namedEntityExtractor = Objects.requireNonNull(namedEntityExtractor);
    }

    @Override
    public void run() {
       this.articleDAO.fetchAll().forEach(this::extractAndSave);
    }

    private void extractAndSave(Article article) {
        this.namedEntityExtractor.extractNames(article)
            .forEach(entity -> this.saveToDb(article, entity));
    }

    /**
     * Save entities and their linked articles, each time an entity is encountered we
     * increment the occurrence counter
     * @param article the article the entities were extracted from
     * @param entity the named entity
     */
    private void saveToDb(Article article, NamedEntity entity) {
        var entityId = Utils.sha1(entity.normalizedName());
        this.entityDAO.insert(entityId, entity.name(), entity.entityType(), 1);
        this.entityDAO.link(article.getId(), entityId);
    }
}
