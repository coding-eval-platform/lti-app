package ar.edu.itba.cep.lti_app.web.controller;

import ar.edu.itba.cep.lti.AuthenticationRequest;
import ar.edu.itba.cep.lti.LtiLoginService;
import ar.edu.itba.cep.lti_app.web.dtos.LoginInitiationRequestDto;
import ar.edu.itba.cep.lti_app.web.exceptions.LoginInitiationRequestWithMissingParamsException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Validator;
import java.net.URI;
import java.util.Optional;

/**
 * LTI controller.
 */
@Controller
@RequestMapping("/init-login")
@AllArgsConstructor
public class LtiLoginController {

    // ===========================================================================
    // Constants of the authentication request handler
    // ===========================================================================

    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String LOGIN_HINT_PARAM = "login_hint";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String NONCE_PARAM = "nonce";
    private static final String LTI_MESSAGE_HINT_PARAM = "lti_message_hint";
    private static final String STATE_PARAM = "state";
    /**
     * The "fixed" part of the authentication request {@link URI}. This part of the {@link URI} is always the same
     * (it contains fixed values).
     */
    private static final String FIXED_PART = "&prompt=none&scope=openid&response_type=id_token&response_mode=form_post";


    // ===========================================================================
    // Needed stuff
    // ===========================================================================

    /**
     * The {@link LtiLoginService} to which the LTI messages are routed.
     */
    private final LtiLoginService ltiLoginService;
    /**
     * A {@link Validator} used to validate input data.
     */
    private final Validator validator;


    // ================================================================================================================
    // Endpoints
    // ================================================================================================================

    @GetMapping
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(code = HttpStatus.TEMPORARY_REDIRECT)
    public String loginGet(final LoginInitiationRequestDto loginInitiationRequestDto) {
        if (!validator.validate(loginInitiationRequestDto).isEmpty()) {
            throw new LoginInitiationRequestWithMissingParamsException(loginInitiationRequestDto);
        }
        final var authenticationRequest = ltiLoginService.loginInitiation(loginInitiationRequestDto.toModel());
        final var uri = buildAuthenticationRequestUri(authenticationRequest);

        return "redirect:" + uri.toString();
    }


    // ================================================================================================================
    // Helpers
    // ================================================================================================================

    /**
     * Creates the {@link URI} to which the user must be redirected to continue the authentication flow
     * (i.e the {@link URI} to be accessed to perform the authentication request)
     *
     * @return The created {@link URI}, based on properties of {@code this} instance.
     */
    private static URI buildAuthenticationRequestUri(final AuthenticationRequest request) {
        final var builder = UriComponentsBuilder.fromUriString(request.getEndpoint())
                .queryParam(CLIENT_ID_PARAM, request.getClientId())
                .queryParam(LOGIN_HINT_PARAM, request.getLoginHint())
                .queryParam(REDIRECT_URI_PARAM, request.getRedirectUri())
                .queryParam(NONCE_PARAM, request.getNonce())
                .query(FIXED_PART);
        Optional.ofNullable(request.getLtiMessageHint()).ifPresent(v -> builder.queryParam(LTI_MESSAGE_HINT_PARAM, v));
        Optional.ofNullable(request.getState()).ifPresent(v -> builder.queryParam(STATE_PARAM, v));

        return builder.build().toUri();
    }
}
