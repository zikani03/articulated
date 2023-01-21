package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.kafka.KafkaArticlePublisher;
import spark.Request;
import spark.Response;

import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

public class ArticleKafkaPublisherRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;
    private final KafkaArticlePublisher publisher;

    public ArticleKafkaPublisherRoute(ObjectMapper objectMapper, ArticleDAO articleDAO, KafkaArticlePublisher publisher) {
        super(objectMapper);
        this.articleDAO = articleDAO;
        this.publisher = publisher;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type(APPLICATION_JSON.asString());
        int n = publisher.publish(articleDAO.fetchAll());
        return objectMapper.writeValueAsString(singletonMap("message", String.format("Published %d articles", n)));
    }
}
