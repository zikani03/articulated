package me.zikani.labs.articulated.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import me.zikani.labs.articulated.dao.ArticleDAO;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
public class MeiliSearchTest {

    @Container
    public GenericContainer meiliSearchContainer = new GenericContainer(DockerImageName.parse("getmeili/meilisearch:v1.2"))
            .withExposedPorts(7700);

    Client client;

    final ObjectMapper objectMapper = new ObjectMapper();
    @BeforeEach
    public void setUp() {
        objectMapper.findAndRegisterModules();
        client = new Client(new Config(String.format("http://%s:%d", meiliSearchContainer.getHost(), meiliSearchContainer.getFirstMappedPort()), "masterKey"));
    }

    @Test
    public void testCanIndexWithMeilisearch() throws MeilisearchException, JsonProcessingException, InterruptedException {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:articulated.db");
        jdbi.installPlugin(new SqlObjectPlugin());

        ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);

        // An index is where the documents are stored.
        Index index = client.index("articles");

        index.addDocuments(objectMapper.writeValueAsString(articleDAO.fetchAll()));
        Thread.sleep(10_000);
        var articles = index.search("football");
        if (articles.getHits().isEmpty()) {
            throw new RuntimeException("didn't expect the results to be empty");
        }

        var firstArticle = articles.getHits().get(0);
        if (firstArticle == null) {
            throw new RuntimeException("expected result");
        }

        System.out.println(firstArticle);
    }
}
