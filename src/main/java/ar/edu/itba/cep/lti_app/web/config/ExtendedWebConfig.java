package ar.edu.itba.cep.lti_app.web.config;

import ar.edu.itba.cep.lti_app.web.data_transfer.AuthenticationRequestFormArgumentResolver;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Extended configuration for Spring Web MVC.
 */
@Configuration
@AllArgsConstructor
public class ExtendedWebConfig implements WebMvcConfigurer {
    /**
     * The {@link AuthenticationRequestFormArgumentResolver} to be registered.
     */
    private final AuthenticationRequestFormArgumentResolver authenticationRequestFormArgumentResolver;


    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticationRequestFormArgumentResolver);
    }
}
