package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.natty.DateGroup;
import org.natty.Parser;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonMap;

public class NattyRoute implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        org.natty.Parser p = new Parser();
        ObjectMapper objectMapper = new ObjectMapper();
        response.type("application/json");
        List<DateGroup> dateGroupList = p.parse(request.body());
        List<Object> data = new ArrayList<>();
        dateGroupList.forEach(dg -> {
            data.addAll(dg.getDates());
        });
        return objectMapper.writeValueAsString(singletonMap("data", data));
    }
}
