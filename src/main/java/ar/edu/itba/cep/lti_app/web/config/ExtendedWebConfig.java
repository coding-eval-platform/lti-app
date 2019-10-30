package ar.edu.itba.cep.lti_app.web.config;

import ar.edu.itba.cep.lti_app.web.data_transfer.AuthenticationRequestFormArgumentResolver;
import ar.edu.itba.cep.lti_app.web.data_transfer.LoginInitiationRequestDtoArgumentResolver;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Extended configuration for Spring Web MVC.
 */
@Configuration
@AllArgsConstructor
public class ExtendedWebConfig implements WebMvcConfigurer {

    private final AuthenticationRequestFormArgumentResolver authenticationRequestFormArgumentResolver;
    private final LoginInitiationRequestDtoArgumentResolver loginInitiationRequestDtoArgumentResolver;


    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticationRequestFormArgumentResolver);
        resolvers.add(loginInitiationRequestDtoArgumentResolver);
    }

    /**
     * Creates a {@link FilterRegistrationBean} for a {@link ForwardedHeaderFilter}, in order to process the
     * Forwarded and X-Forwarded-* headers.
     *
     * @return The {@link FilterRegistrationBean}.
     */
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        final var bean = new FilterRegistrationBean<ForwardedHeaderFilter>();
        bean.setFilter(new ForwardedHeaderFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }
}
