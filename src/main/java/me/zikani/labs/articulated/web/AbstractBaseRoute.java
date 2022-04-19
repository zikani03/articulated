package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Route;

public abstract class AbstractBaseRoute implements Route {
    protected final ObjectMapper objectMapper;

    public AbstractBaseRoute(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
