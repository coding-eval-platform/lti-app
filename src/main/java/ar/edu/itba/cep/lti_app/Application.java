package ar.edu.itba.cep.lti_app;

import ar.edu.itba.cep.lti.config.EnableLtiService;
import lombok.Data;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Main class.
 */
@SpringBootApplication
@EnableCircuitBreaker
@EnableLtiService
@EnableConfigurationProperties(Application.Properties.class)
public class Application {

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
     * Entry point.
     *
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .bannerMode(Banner.Mode.OFF)
                .build().run(args);
    }

    /**
     * Created by Juan Marcos Bellini on 2019-10-27.
     */
    @Data
    @ConfigurationProperties("lti-app")
    public static class Properties {
        /**
         * The url template at which the "exam taking" feature is deployed.
         */
        private String examTakingUrlTemplate;
    }
}
