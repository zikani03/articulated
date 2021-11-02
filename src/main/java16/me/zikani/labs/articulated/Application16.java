package me.zikani.labs.articulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import spark.EmbeddedJettyFactoryConstructor;
import spark.Spark;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;

import static java.util.Collections.singletonMap;

public class Application {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void createServerWithRequestLog(Logger logger) {
        EmbeddedJettyFactory factory = createEmbeddedJettyFactoryWithRequestLog();
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, factory);
    }

    private static EmbeddedJettyFactory createEmbeddedJettyFactoryWithRequestLog() {
        return new EmbeddedJettyFactoryConstructor(null).create();
    }

    public static void main(String... args) {
        createServerWithRequestLog(null);
        Spark.get("/articles", (request, response) -> {
            return objectMapper.writeValueAsString(singletonMap("artiles", "No articles here"));
        });
    }
}
