package me.zikani.labs.articulated.ai.articleembeddings;

import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.output.FinishReason;
import me.zikani.labs.articulated.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticleEmbeddingGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ArticleEmbeddingGenerator.class);
    private final OllamaEmbeddingModel embeddingModel;

    public ArticleEmbeddingGenerator(OllamaEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * Generates article embeddings suitable for use for clustering, search and retrieval
     * @param article article to generate embeddings for
     * @return vector embeddings
     */
    public float[] generateEmbeddings(Article article) {
        if (article == null) {
            throw new IllegalArgumentException("article cannot be null");
        }
        if (article.getBody() == null || article.getBody().isEmpty()) {
            throw new IllegalArgumentException("article content cannot be null or empty");
        }
        var embeddingResponse = embeddingModel.embed(article.getBody());
        return embeddingResponse.content().vector();
    }

    public float[] generateEmbeddingsFromString(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("article content cannot be null or empty");
        }
        var embeddingResponse = embeddingModel.embed(content);
        return embeddingResponse.content().vector();
    }
}
