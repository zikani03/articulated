package me.zikani.labs.articulated.greypot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;

@Data
public class ExportResponse {
    @JsonProperty("reportId")
    private String reportId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private String data;

    public byte[] dataAsByteArray() {
        return Base64.decodeBase64(this.data);
    }
}
