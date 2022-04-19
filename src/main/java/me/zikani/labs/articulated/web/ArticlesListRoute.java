package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import spark.Request;
import spark.Response;
import spark.Route;

import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

public class ArticlesListRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;

    public ArticlesListRoute(ObjectMapper objectMapper, ArticleDAO articleDAO) {
        super(objectMapper);
        this.articleDAO = articleDAO;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type(APPLICATION_JSON.asString());
        return objectMapper.writeValueAsString(singletonMap("articles", articleDAO.fetchAll()));
    }
}
