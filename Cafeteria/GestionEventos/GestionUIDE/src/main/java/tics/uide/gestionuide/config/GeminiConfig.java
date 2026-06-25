package tics.uide.gestionuide.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate dedicado a Gemini, con timeouts de conexión y lectura (aislamiento: si Gemini cuelga,
 * la petición corta y degrada). Inyectable por @Qualifier("geminiRestTemplate") para poder atarle
 * un MockRestServiceServer en los tests sin red.
 */
@Configuration
public class GeminiConfig {

    @Bean("geminiRestTemplate")
    public RestTemplate geminiRestTemplate(@Value("${gemini.timeout-ms:8000}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }
}
