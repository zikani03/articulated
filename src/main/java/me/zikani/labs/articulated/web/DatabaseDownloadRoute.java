package me.zikani.labs.articulated.web;

import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseDownloadRoute implements Route {
    private final String databasePath;

    public DatabaseDownloadRoute(String databasePath) {
        this.databasePath = databasePath;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("binary/octet-stream");
        try(OutputStream os = response.raw().getOutputStream()) {
            os.write(Files.readAllBytes(Paths.get(databasePath)));
            response.status(HttpStatus.OK_200);
        } catch (IOException e) {
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return response;
    }
}
