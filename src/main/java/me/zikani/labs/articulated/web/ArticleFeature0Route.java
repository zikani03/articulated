package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.model.Article;
import spark.Request;
import spark.Response;

import java.util.stream.Collectors;

public class ArticleFeature0Route extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;

    public ArticleFeature0Route(ObjectMapper objectMapper, ArticleDAO articleDAO) {
        super(objectMapper);
        this.articleDAO = articleDAO;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        var features = articleDAO.fetchAll()
                .stream().map(Article::makeFeature)
                .map(Article.ArticleFeature0::getFeatures)
                .collect(Collectors.toList());

        return json(response, features);
    }
}
