package ar.edu.itba.cep.lti_app.web.controller;

import ar.edu.itba.cep.lti.*;
import ar.edu.itba.cep.lti_app.Application;
import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import ar.edu.itba.cep.lti_app.web.dtos.ExamSelectedForm;
import ar.edu.itba.cep.lti_app.web.dtos.LoginInitiationRequestDto;
import ar.edu.itba.cep.lti_app.web.exceptions.AuthenticationResponseWithMissingParamsException;
import ar.edu.itba.cep.lti_app.web.exceptions.LoginInitiationRequestWithMissingParamsException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.Validator;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * LTI controller.
 */
@Controller
@RequestMapping("lti/app")
@AllArgsConstructor
public class LtiController implements InitializingBean {

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
    // Exam taking url template variables
    // ===========================================================================

    private static final String EXAM_ID_VARIABLE = "exam-id";
    private static final String ACCESS_TOKEN_VARIABLE = "access-token";
    private static final String REFRESH_TOKEN_VARIABLE = "refresh-token";
    private static final String TOKEN_ID_VARIABLE = "token-id";


    // ===========================================================================
    // Needed stuff
    // ===========================================================================

    /**
     * The {@link LtiService} to which the LTI messages are routed.
     */
    private final LtiService ltiService;
    /**
     * A {@link Validator} used to validate input data.
     */
    private final Validator validator;
    /**
     * An {@link Application.Properties} instance used to configure behaviour of this controller.
     */
    private final Application.Properties properties;


    // ================================================================================================================
    // Initializing bean
    // ================================================================================================================

    @Override
    public void afterPropertiesSet() {
        validateProperties();
    }

    // ================================================================================================================
    // Endpoints
    // ================================================================================================================

    // ================ Login initiation ================

    @RequestMapping(value = "init-login")
    @GetMapping
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(code = HttpStatus.TEMPORARY_REDIRECT)
    public String loginGet(final LoginInitiationRequestDto loginInitiationRequestDto) {
        if (!validator.validate(loginInitiationRequestDto).isEmpty()) {
            throw new LoginInitiationRequestWithMissingParamsException(loginInitiationRequestDto);
        }
        final var authenticationRequest = ltiService.loginInitiation(loginInitiationRequestDto.toModel());
        final var uri = buildAuthenticationRequestUri(authenticationRequest);

        return "redirect:" + uri.toString();
    }


    // ================= Exam selection =================

    @PostMapping(value = "create-exam", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(code = HttpStatus.SEE_OTHER)
    public String examSelection(final AuthenticationResponseForm form) {
        return handleLtiMessage(form, this::examSelection);
    }

    @GetMapping(value = "exam-selection")
    public String examSelection(
            @RequestParam(value = "state") final String state,
            @RequestParam(value = "examId", required = false) final Long examId,
            final Model model) {
        final var examSelectedForm = new ExamSelectedForm(examId, state);
        model.addAttribute("examSelectionForm", examSelectedForm);

        return "exam-selection";
    }

    @PostMapping(value = "exam-selected", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String examSelected(
            @Valid @ModelAttribute("examSelectionForm") final ExamSelectedForm form,
            final BindingResult result,
            final Model model) {
        if (result.hasErrors()) {
            return "exam-selection";
        }

        final var url = MvcUriComponentsBuilder.fromController(LtiController.class)
                .path("/take-exam")
                .build()
                .toString();


        final var request = new ExamSelectedRequest(form.getExamId(), form.getState(), url, null, null);
        final var response = ltiService.examSelected(request);
        model.addAttribute("examId", form.getExamId());
        if (response instanceof NonExistingExamSelectedResponse) {
            model.addAttribute("cause", "non-existing");
            return "exam-with-error";
        }
        if (response instanceof NotUpcomingExamSelectedResponse) {
            model.addAttribute("cause", "not-upcoming");
            return "exam-with-error";
        }
        final var existingExamSelectedResponse = (ExistingExamSelectedResponse) response;
        model.addAttribute("endpoint", existingExamSelectedResponse.getEndpoint());
        model.addAttribute("jwt", existingExamSelectedResponse.getJwt());
        model.addAttribute("examData", existingExamSelectedResponse.getExamData());
        return "exam-selected";
    }


    // ================== Exam taking ===================

    @PostMapping(value = "take-exam", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(code = HttpStatus.SEE_OTHER)
    public String takeExam(final AuthenticationResponseForm form) {
        return handleLtiMessage(form, this::takeExam);
    }

    @GetMapping(value = "take-exam")
    public String takeExam(
            @RequestParam("examId") final Long examId,
            @RequestParam("tokenId") final UUID tokenId,
            @RequestParam("accessToken") final String accessToken,
            @RequestParam("refreshToken") final String refreshToken,
            @RequestParam("returnUrl") final String returnUrl,
            final Model model) {
        model.addAttribute("examId", examId);
        final var valueMap = Map.of(
                EXAM_ID_VARIABLE, examId,
                TOKEN_ID_VARIABLE, tokenId,
                ACCESS_TOKEN_VARIABLE, accessToken,
                REFRESH_TOKEN_VARIABLE, refreshToken
        );
        final var examTakingUrl = StrSubstitutor.replace(properties.getExamTakingUrlTemplate(), valueMap);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("examTakingUrl", examTakingUrl);

        return "take-exam";
    }


    // ================================================================================================================
    // Helpers
    // ================================================================================================================

    /**
     * Handles an Lti authentication response message.
     *
     * @param form              The {@link AuthenticationResponseForm} carrying the received data (i.e state and ID token).
     * @param ltiMessageHandler An {@link LtiMessageHandler} with the action to be performed in case the
     *                          {@link AuthenticationResponseForm} carries valid data.
     *                          The result will be appended to the "redirect:" prefix.
     * @return The result of the {@link LtiMessageHandler} if the {@link AuthenticationResponseForm} carries valid data.
     * @throws AuthenticationResponseWithMissingParamsException If the {@code form} contains errors.
     */
    private String handleLtiMessage(final AuthenticationResponseForm form, final LtiMessageHandler ltiMessageHandler) {
        if (!validator.validate(form).isEmpty()) {
            throw new AuthenticationResponseWithMissingParamsException(form);
        }
        return "redirect:" + ltiMessageHandler.handle(form.toAuthenticationResponse());
    }

    /**
     * Handles the exam selection request.
     *
     * @param response The {@link AuthenticationResponse} representing an exam selection deep linking request.
     * @return A {@link String} representing the view to be shown.
     */
    private String examSelection(final AuthenticationResponse response) {
        return "exam-selection?state=" + ltiService.examSelection(response).getState();
    }

    /**
     * Handles the exam take request.
     *
     * @param response The {@link AuthenticationResponse} representing an exam selection resource link launch request.
     * @return A {@link String} representing the view to be shown.
     */
    private String takeExam(final AuthenticationResponse response) {
        final var examTakingResponse = ltiService.takeExam(response);
        return "take-exam"
                + "?examId=" + examTakingResponse.getExamId()
                + "&tokenId=" + examTakingResponse.getTokenId()
                + "&accessToken=" + examTakingResponse.getAccessToken()
                + "&refreshToken=" + examTakingResponse.getRefreshToken()
                + "&returnUrl=" + URLEncoder.encode(examTakingResponse.getReturnUrl(), StandardCharsets.UTF_8)
                ;

    }

    /**
     * Asserts that the {@link #properties} are valid.
     *
     * @throws IllegalArgumentException If the {@link #properties} are not valid.
     */
    private void validateProperties() throws IllegalArgumentException {
        final var examTakingUrlTemplate = properties.getExamTakingUrlTemplate();
        Assert.notNull(examTakingUrlTemplate, "The \"exam taking\" url template must not be null");
        Assert.isTrue(
                examTakingUrlTemplate.contains("${" + EXAM_ID_VARIABLE + "}"),
                "The \"exam taking\" url template must contain the exam id variable (" + EXAM_ID_VARIABLE + ")"
        );
        Assert.isTrue(
                examTakingUrlTemplate.contains("${" + ACCESS_TOKEN_VARIABLE + "}"),
                "The \"exam taking\" url template must contain the access token variable (" + ACCESS_TOKEN_VARIABLE + ")"
        );
        Assert.isTrue(
                examTakingUrlTemplate.contains("${" + REFRESH_TOKEN_VARIABLE + "}"),
                "The \"exam taking\" url template must contain the refresh token variable (" + REFRESH_TOKEN_VARIABLE + ")"
        );
        Assert.isTrue(
                examTakingUrlTemplate.contains("${" + TOKEN_ID_VARIABLE + "}"),
                "The \"exam taking\" url template must contain the token id variable (" + TOKEN_ID_VARIABLE + ")"
        );
    }


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


    /**
     * A functional interface that defines a method to handle LTI messages.
     * This method will be called by {@link #handleLtiMessage(AuthenticationResponseForm, LtiMessageHandler)}
     * if the {@link AuthenticationResponseForm} is valid.
     * The result of this method will be appended to the "redirect:" prefix.
     */
    @FunctionalInterface
    private interface LtiMessageHandler {

        /**
         * Handles the given {@code response}.
         *
         * @param response The {@link AuthenticationResponse} to be handled.
         * @return A {@link String} representing the redirection to be performed.
         */
        String handle(final AuthenticationResponse response);
    }
}
