package me.zikani.labs.articulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Spark;

import static java.util.Collections.singletonMap;

public class Application {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String... args) {
        Spark.get("/articles", (request, response) -> {
            return objectMapper.writeValueAsString(singletonMap("artiles", "No articles here"));
        });
    }
}
