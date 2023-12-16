package me.zikani.labs.articulated.articleworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.Utils;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.EntityDAO;
import me.zikani.labs.articulated.nlp.NamedEntityRecognition;

public class NamedEntityResolverWorker implements Runnable {

    private final ArticleDAO articleDAO;
    private final EntityDAO entityDAO;
    private final NamedEntityRecognition namedEntityRecognition;

    private final ObjectMapper objectMapper;

    public NamedEntityResolverWorker(ArticleDAO articleDAO,
                                     EntityDAO entityDAO,
                                     NamedEntityRecognition namedEntityRecognition,
                                     ObjectMapper objectMapper) {
        this.articleDAO = articleDAO;
        this.entityDAO = entityDAO;
        this.namedEntityRecognition = namedEntityRecognition;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        articleDAO.fetchAll().stream()
            .forEach(article -> {
                var entities = namedEntityRecognition.extractNames(article);

                // Save entities, incrementing the counter for each...
                // TODO: filter some entity names...
                entities.forEach(entity -> {
                    var entityId = Utils.sha1(entity.normalizedName());
                    entityDAO.insert(entityId, entity.name(), entity.entityType(), 1);
                    entityDAO.link(article.getId(), entityId);
                });
            });
    }
}
