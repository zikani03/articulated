package me.zikani.labs.articulated.search;

import me.zikani.labs.articulated.dao.ArticleDAO;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.CommandObjects;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.ProtocolCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
public class RedisIndexTest {

    @Container
    public GenericContainer redisC = new GenericContainer(DockerImageName.parse("redis:7.2.0-alpine"))
            .withExposedPorts(6379);

    JedisPool pool;

    @BeforeEach
    public void setUp() {
        pool = new JedisPool(redisC.getHost(), redisC.getFirstMappedPort());
    }

    @Test
    public void testCanSetArticlesOnRedis() {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:articulated.db");
        jdbi.installPlugin(new SqlObjectPlugin());

        ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);

        try (Jedis redis = pool.getResource()) {
            articleDAO.fetchAll().forEach(article -> {
                redis.set(article.getId(), article.getBody());
            });


            var articles = articleDAO.searchArticles("Football");
            if (articles.isEmpty()) {
                throw new RuntimeException("didn't expect the results to be empty");
            }

            var firstArticle = articles.get(0);

            String result = redis.get(firstArticle.getId());
            if (result == null) {
                throw new RuntimeException("expected result");
            }

            assertEquals(result, firstArticle.getBody());
        }
    }


    @Test
    public void testCanTriggerFunctionToNormalizeTitle() {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:articulated.db");
        jdbi.installPlugin(new SqlObjectPlugin());

        ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);


        try (Jedis redis = pool.getResource()) {

            var arguments = new ProtocolCommand() {
                @Override
                public byte[] getRaw() {
                    String cmd = "LOAD REPLACE \"#!js name=testLibrary api_version=1.0\\n\s" +
                            "function normalizeArticleTitle(client, data) {\n" +
                            "client.call('set', data.key, data.value.toLower());\n " +
                            "}\n" +
                            "redis.registerKeySpaceTrigger('normalizeTitle', 'article:', normalizeArticleTitle);\"";

                    return cmd.getBytes();
                }
            };
            redis.getConnection().executeCommand(new CommandArguments(
                    new ProtocolCommand() {
                        @Override
                        public byte[] getRaw() {
                            return "TFUNCTION ".getBytes();
                        }
                    }).add(arguments));

            var articles = articleDAO.fetchAll();
            articles.forEach(article -> {
                redis.set(String.format("articles:%s:title", article.getId()), article.getTitle());
            });

            var firstArticle = articles.get(0);

            String result = redis.get(String.format("articles:%s:title", firstArticle.getId()));
            if (result == null) {
                throw new RuntimeException("expected result");
            }

            assertEquals(firstArticle.getTitle().toLowerCase(), result);
        }
    }
}
