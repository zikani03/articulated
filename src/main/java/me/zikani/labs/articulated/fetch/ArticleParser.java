 package me.zikani.labs.articulated.fetch;

import me.zikani.labs.articulated.model.Article;

import java.util.List;

public interface ArticleParser {
    String getCategoryPageUrl(String category, int page);

    Article parseArticle(String body, Article article);

    List<Article> followArticleLinks(String categoryPageBody);
}
