package me.zikani.labs.articulated.greypot;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
public class GeneratePDFRequest {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Template")
    private String template;

    @JsonProperty("Data")
    private Object data;

    public GeneratePDFRequest(String name, String template, Object data) {
        this.name = name;
        this.template = template;
        this.data = data;
    }
}
