package me.zikani.labs.articulated.ai.articleembeddings;

import io.github.jbellis.jvector.graph.*;
import io.github.jbellis.jvector.graph.similarity.BuildScoreProvider;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.DefaultVectorizationProvider;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import me.zikani.labs.articulated.dao.ArticleDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmbeddingDatabase {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingDatabase.class);
    private final ArticleEmbeddingGenerator articleEmbeddingGenerator;
    private final ArticleDAO articleDAO;
    private OnHeapGraphIndex index;
    private ListRandomAccessVectorValues ravv;
    private Map<Integer, String> articleIndex = new HashMap<>();

    public EmbeddingDatabase(ArticleEmbeddingGenerator articleEmbeddingGenerator, ArticleDAO articleDAO) {
        this.articleEmbeddingGenerator = articleEmbeddingGenerator;
        this.articleDAO = articleDAO;
    }

    List<VectorFloat<?>> generateArticleVectors() {
        var listVectors = new ArrayList<VectorFloat<?>>();
        var articles = articleDAO.fetchLimited();
        for (int i = 0; i < articles.size(); i++) {
            var article = articles.get(i);
            var embeddings = articleEmbeddingGenerator.generateEmbeddings(article);
            articleIndex.put(i, article.getTitle());
            logger.info("generating embeddings for article {}", article.getTitle());
            var a = makeVectorFloat(embeddings);
            listVectors.add(a);
        }
        return listVectors;
    }

    public void initialize() {
        var baseVectors = generateArticleVectors();
        // infer the dimensionality from the first vector
        int originalDimension = 768;
        // wrap the raw vectors in a RandomAccessVectorValues
        this.ravv = new ListRandomAccessVectorValues(baseVectors, originalDimension);

        // score provider using the raw, in-memory vectors
        BuildScoreProvider bsp = BuildScoreProvider.randomAccessScoreProvider(ravv, VectorSimilarityFunction.EUCLIDEAN);
        try (GraphIndexBuilder builder = new GraphIndexBuilder(bsp,
                ravv.dimension(),
                16, // graph degree
                100, // construction search depth
                1.2f, // allow degree overflow during construction by this factor
                1.2f, // relax neighbor diversity requirement by this factor (alpha)
                true)) // use a hierarchical index
        {
            // build the index (in memory)
            this.index = builder.build(ravv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object search(String queryStr) throws Exception {
        var queryEmbedding = articleEmbeddingGenerator.generateEmbeddingsFromString(queryStr);
        // search for a random vector
        VectorFloat<?> q = makeVectorFloat(queryEmbedding);
        SearchResult sr = GraphSearcher.search(q,
                10, // number of results
                ravv, // vectors we're searching, used for scoring
                VectorSimilarityFunction.EUCLIDEAN, // how to score
                index,
                Bits.ALL); // valid ordinals to consider

        List<Object> results = new ArrayList<>();
        for (SearchResult.NodeScore ns : sr.getNodes()) {
            results.add(articleIndex.get(ns.node));
        }
        return results;
    }

    public static VectorFloat<?> makeVectorFloat(float[] data) {
        return DefaultVectorizationProvider.getInstance().getVectorTypeSupport().createFloatVector(data);
    }
}
