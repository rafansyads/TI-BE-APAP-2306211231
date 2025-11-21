package apap.ti._5.accommodation_2306211231_be.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "backend")
public class BackendServicesProperties {
    /**
     * Map of logical service name -> base URL, e.g.
     * backend.services.backend2: https://backend2.example
     * backend.services.user: https://user.example
     */
    private Map<String, String> services = new HashMap<>();

    public Map<String, String> getServices() {
        return services;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }
}
