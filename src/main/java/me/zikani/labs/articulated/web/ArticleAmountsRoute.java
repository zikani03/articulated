package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.model.Amount;
import me.zikani.labs.articulated.model.Article;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

public class ArticleAmountsRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;

    public ArticleAmountsRoute(ObjectMapper objectMapper, ArticleDAO articleDAO) {
        super(objectMapper);
        this.articleDAO = articleDAO;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type(APPLICATION_JSON.asString());
        List<Article> articleList =  articleDAO.fetchAll();
        Map<String, List<Amount>> amounts = new HashMap<>();

        articleList.forEach(article -> {
            amounts.put(article.getUrl(), article.getMentionedAmounts());
        });

        return objectMapper.writeValueAsString(singletonMap("articles", amounts));
    }
}
