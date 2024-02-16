package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.kafka.KafkaArticlePublisher;
import org.jetbrains.annotations.NotNull;

import static java.util.Collections.singletonMap;

public class ArticleKafkaPublisherRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;
    private final KafkaArticlePublisher publisher;

    public ArticleKafkaPublisherRoute(ObjectMapper objectMapper, ArticleDAO articleDAO, KafkaArticlePublisher publisher) {
        super(objectMapper);
        this.articleDAO = articleDAO;
        this.publisher = publisher;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        int n = publisher.publish(articleDAO.fetchAll());
        context.json(singletonMap("message", String.format("Published %d articles", n)));
    }
}
