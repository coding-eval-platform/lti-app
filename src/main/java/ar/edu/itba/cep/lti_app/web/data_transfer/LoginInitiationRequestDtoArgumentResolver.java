package ar.edu.itba.cep.lti_app.web.data_transfer;

import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import ar.edu.itba.cep.lti_app.web.dtos.LoginInitiationRequestDto;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * A {@link HandlerMethodArgumentResolver} that can create {@link AuthenticationResponseForm} instances
 * according to the {@link #ISSUER_PROPERTY} and {@link #LOGIN_HINT_PROPERTY} field names.
 *
 * @see LoginInitiationRequestDto
 */
@Component
public class LoginInitiationRequestDtoArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * The name of the field for the issuer.
     */
    private static final String ISSUER_PROPERTY = "iss";
    /**
     * The name of the field for the login hint.
     */
    private static final String LOGIN_HINT_PROPERTY = "login_hint";
    /**
     * The name of the field for the target link uri.
     */
    private static final String TARGET_LINK_URI_PROPERTY = "target_link_uri";
    /**
     * The name of the field for the lti message hint.
     */
    private static final String LTI_MESSAGE_HINT_PROPERTY = "lti_message_hint";
    /**
     * The name of the field for the deployment id.
     */
    private static final String DEPLOYMENT_ID_PROPERTY = "lti_deployment_id";
    /**
     * The name of the field for the client id.
     */
    private static final String CLIENT_ID_PROPERTY = "client_id";


    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType() == LoginInitiationRequestDto.class;
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory) {
        return new LoginInitiationRequestDto(
                webRequest.getParameter(ISSUER_PROPERTY),
                webRequest.getParameter(LOGIN_HINT_PROPERTY),
                webRequest.getParameter(TARGET_LINK_URI_PROPERTY),
                webRequest.getParameter(LTI_MESSAGE_HINT_PROPERTY),
                webRequest.getParameter(DEPLOYMENT_ID_PROPERTY),
                webRequest.getParameter(CLIENT_ID_PROPERTY)
        );
    }
}
