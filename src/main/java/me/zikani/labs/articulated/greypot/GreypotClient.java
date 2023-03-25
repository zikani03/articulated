package me.zikani.labs.articulated.greypot;

import java.util.Optional;

public interface GreypotClient {

    /**
     *
     * @param templateId template id
     * @param templateContent template
     * @param data data
     * @return report response
     */
    Optional<ExportResponse> generatePDF(String templateId, String templateContent, Object data) throws Exception;
}
