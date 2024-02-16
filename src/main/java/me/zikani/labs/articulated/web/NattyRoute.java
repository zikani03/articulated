package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.natty.DateGroup;
import org.natty.Parser;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonMap;

public class NattyRoute implements Handler {

    @Override
    public void handle(@NotNull Context context) throws Exception {
        org.natty.Parser p = new Parser();
        ObjectMapper objectMapper = new ObjectMapper();
        List<DateGroup> dateGroupList = p.parse(context.body());
        List<Object> data = new ArrayList<>();
        dateGroupList.forEach(dg -> {
            data.addAll(dg.getDates());
        });
        context.json(singletonMap("data", data));
    }
}
