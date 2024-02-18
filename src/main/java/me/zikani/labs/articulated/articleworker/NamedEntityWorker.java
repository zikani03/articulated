package me.zikani.labs.articulated.articleworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import me.zikani.labs.articulated.Utils;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.dao.EntityDAO;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.nlp.NamedEntity;
import me.zikani.labs.articulated.nlp.NamedEntityExtractor;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NamedEntityWorker implements Runnable {

    private final ArticleDAO articleDAO;
    private final EntityDAO entityDAO;
    private final NamedEntityExtractor namedEntityExtractor;

    private final Jedis redisClient;
    private ObjectMapper objectMapper;


    public NamedEntityWorker(final ArticleDAO articleDAO,
                             final EntityDAO entityDAO,
                             final NamedEntityExtractor namedEntityExtractor,
                             final JedisPool jedisPool,
                             final ObjectMapper objectMapper) {
        this.articleDAO = Objects.requireNonNull(articleDAO);
        this.entityDAO = Objects.requireNonNull(entityDAO);
        this.namedEntityExtractor = Objects.requireNonNull(namedEntityExtractor);
        this.redisClient = jedisPool.getResource();
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
       this.articleDAO.fetchAll().forEach(this::extractAndSave);
    }

    private void extractAndSave(Article article) {
        try {
            var entities = this.namedEntityExtractor.extractNames(article);
            entities.forEach(entity -> this.saveToDb(article, entity));
            // We index the article, and it's found entities, we expect the count to be set by Redis trigger function
            var entitiesArr = entities.stream().map(NamedEntity::name).collect(Collectors.toList());
            var articleKey = "article:" + article.getId();
            redisClient.hset(articleKey, "entities", objectMapper.writeValueAsString(entitiesArr));
            LoggerFactory.getLogger(getClass()).info("Indexed into redis key={}", articleKey);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("failed to extractAndSave", e);
        }
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
        var entityKey = String.format("entity:%s", entityId);
        // Store number of occurrences for this entity
        redisClient.incr(entityKey);
    }
}
