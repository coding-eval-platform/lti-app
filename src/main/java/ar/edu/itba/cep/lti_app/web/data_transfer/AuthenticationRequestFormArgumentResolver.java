package ar.edu.itba.cep.lti_app.web.data_transfer;

import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * A {@link HandlerMethodArgumentResolver} that can create {@link AuthenticationResponseForm} instances
 * according to the {@link #STATE_NAME} and {@link #ID_TOKEN_NAME} field names.
 *
 * @see AuthenticationResponseForm
 */
@Component
public class AuthenticationRequestFormArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * The name of the field for the state.
     */
    private static final String STATE_NAME = "state";
    /**
     * The name of the field for the id token.
     */
    private static final String ID_TOKEN_NAME = "id_token";


    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType() == AuthenticationResponseForm.class;
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory) {
        return new AuthenticationResponseForm(
                webRequest.getParameter(ID_TOKEN_NAME),
                webRequest.getParameter(STATE_NAME)
        );
    }
}
