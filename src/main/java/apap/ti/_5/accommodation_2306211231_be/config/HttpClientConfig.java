package apap.ti._5.accommodation_2306211231_be.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for HTTP clients to communicate with backend services.
 * This class creates a map of WebClient instances, one for each backend service
 * defined in BackendServicesProperties.
 * 
 * This also allows backward compatibility by providing a bean named "backend2WebClient"
 * for existing code that expects it.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public Map<String, WebClient> backendWebClients(WebClient.Builder builder,
                                                    BackendServicesProperties backendServicesProperties) {
        Map<String, WebClient> clients = new HashMap<>();
        backendServicesProperties.getServices().forEach((name, url) -> {
            if (url != null && !url.isBlank()) {
                WebClient client = builder
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(5))))
                    .build();
                clients.put(name, client);
            }
        });
        return clients;
    }

    /**
     * Backward compatible single bean for previous code expecting a bean named backend2WebClient.
     * If backend.services.backend2 is not defined, returns a generic WebClient without baseUrl.
     */
    @Bean(name = "backend2WebClient")
    @ConditionalOnMissingBean(name = "backend2WebClient")
    public WebClient backend2WebClient(WebClient.Builder builder,
                                       BackendServicesProperties backendServicesProperties) {
        String url = backendServicesProperties.getServices().get("backend2");
        WebClient.Builder b = builder
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create().responseTimeout(Duration.ofSeconds(5))));
        if (url != null && !url.isBlank()) {
            b.baseUrl(url);
        }
        return b.build();
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(3))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    }
}
