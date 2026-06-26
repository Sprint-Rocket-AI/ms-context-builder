package cl.sprint_rocket_ai.ms_context_builder.ai_index.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Configuración del {@link RestClient} usado por el adapter de indexación AI.
 */
@Configuration
public class AIIndexRestClientConfig {

    @Bean("aiIndexRestClient")
    public RestClient aiIndexRestClient(@Value("${ai-index.client.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
