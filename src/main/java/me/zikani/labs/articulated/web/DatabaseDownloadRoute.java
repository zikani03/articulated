/**
 * MIT License
 *
 * Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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