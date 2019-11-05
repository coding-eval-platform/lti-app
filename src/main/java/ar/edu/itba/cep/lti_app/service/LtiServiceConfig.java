package ar.edu.itba.cep.lti_app.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for the LTI Service integration.
 */
@Configuration
@EnableConfigurationProperties(LtiServiceConfig.Properties.class)
class LtiServiceConfig {

    /**
     * A load balanced {@link RestTemplate}.
     *
     * @param restTemplateBuilder The {@link RestTemplateBuilder} used to create the {@link RestTemplate} instance.
     * @return The created {@link RestTemplate}.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    /**
     * Configuration properties for the {@link LtiService}.
     */
    @Data
    @ConfigurationProperties("lti-service")
    /* package */ static final class Properties {
        /**
         * The base url where the LTI is serving.
         */
        private String baseUrl = "http://lti-service/lti/app/";
    }
}
