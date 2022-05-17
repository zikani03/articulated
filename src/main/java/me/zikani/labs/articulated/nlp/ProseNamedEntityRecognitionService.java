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
package me.zikani.labs.articulated.nlp;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zikani.labs.articulated.model.Article;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProseNamedEntityRecognitionService implements NamedEntityRecognition {
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ProseNamedEntityRecognitionService(String apiUrl, ObjectMapper objectMapper) {
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public List<NamedEntity> extractNames(final Article article) {

        List<NamedEntity> namedEntities = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.apiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(article.getBody()))
                    .build();
            byte[] json = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
            ProseServiceResponse response = objectMapper.readValue(json, ProseServiceResponse.class);
            namedEntities.addAll(response.getEntities());
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Failed to extract entities", e);
        }
        return namedEntities;
    }

    private static class ProseServiceResponse {
        List<NamedEntity> entities;

        public List<NamedEntity> getEntities() {
            return entities;
        }

        public void setEntities(List<NamedEntity> entities) {
            this.entities = entities;
        }
    }
}
