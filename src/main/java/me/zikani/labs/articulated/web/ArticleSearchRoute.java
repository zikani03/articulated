package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.model.Article;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

/**
 * Implements a simple full-text search for articles using SQLites Full-Text Search capabilities..
 *
 */
public class ArticleSearchRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;

    public ArticleSearchRoute(ObjectMapper objectMapper, ArticleDAO articleDAO) {
        super(objectMapper);
        this.articleDAO = articleDAO;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type(APPLICATION_JSON.asString());
        String query = request.queryParams("q");
        if (query == null || query.isEmpty()) {
            return objectMapper.writeValueAsBytes(emptyList());
        }
        List<Article> articleList = articleDAO.searchArticles(query);
        return objectMapper.writeValueAsBytes(articleList);
    }
}
