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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
public class RedisIndexTest {

//    @Container
//    public GenericContainer zincSearch = new GenericContainer(DockerImageName.parse("zincsearch:5.0.3-alpine"))
//            .withExposedPorts(6379);
    @Container
    public GenericContainer redisC = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379);

    JedisPool pool;

    @BeforeEach
    public void setUp() {
        pool = new JedisPool(redisC.getHost(), redisC.getFirstMappedPort());
    }

    @Test
    public void testCanIndexWithZinc() {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:articulated.db");
        jdbi.installPlugin(new SqlObjectPlugin());

        ArticleDAO articleDAO = jdbi.onDemand(ArticleDAO.class);

        try (Jedis redis =pool.getResource()) {
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
}
