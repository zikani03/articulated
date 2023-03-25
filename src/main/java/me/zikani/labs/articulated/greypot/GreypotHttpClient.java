package me.zikani.labs.articulated.greypot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

public class GreypotHttpClient implements GreypotClient {

    private final String baseApiURL;
    private final ObjectMapper objectMapper;

    public GreypotHttpClient(String apiURL, ObjectMapper objectMapper) {
        this.baseApiURL = apiURL;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ExportResponse> generatePDF(String templateId, String templateContent, Object data) throws JsonProcessingException {
        GeneratePDFRequest request = new GeneratePDFRequest(templateId, templateContent, data);

        String url = baseApiURL + "/_studio/generate/pdf/%s";

        String requestBody = objectMapper.writeValueAsString(request);
        LoggerFactory.getLogger(getClass()).trace("Sending request body {}", requestBody);
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(String.format(url, request.getName())))
                .header("Content-Type", APPLICATION_JSON.asString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> respBody = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            ExportResponse response = objectMapper.readValue(respBody.body(), ExportResponse.class);

            return Optional.of(response);
        } catch (Exception ioe) {
            LoggerFactory.getLogger(getClass()).error("failed to send request", ioe);
        }
        return Optional.empty();
    }
}
