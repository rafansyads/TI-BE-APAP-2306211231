package apap.ti._5.accommodation_2306211231_be.restdto.request.auth;

import java.util.Map;

/**
 * DTO used by AuthRestController to forward the authenticated user to an external
 * service by rendering an auto-submitting HTML POST form.
 */
public class ForwardRequestDTO {
    private String targetUrl;
    private Map<String, String> params;

    public ForwardRequestDTO() {}

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
