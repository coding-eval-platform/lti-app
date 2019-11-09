package ar.edu.itba.cep.lti_app;

import lombok.Data;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main class.
 */
@SpringBootApplication
@EnableConfigurationProperties(Application.Properties.class)
public class Application {

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
     * Bean class containing application level properties.
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
