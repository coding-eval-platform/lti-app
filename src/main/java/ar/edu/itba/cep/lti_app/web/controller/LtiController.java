package ar.edu.itba.cep.lti_app.web.controller;

import ar.edu.itba.cep.lti.*;
import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import ar.edu.itba.cep.lti_app.web.dtos.ExamSelectedForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.Validator;
import java.net.URI;
import java.util.Optional;

/**
 * LTI controller.
 */
@Controller
@RequestMapping("lti/app")
public class LtiController {

    /**
     * The "fixed" part of the authentication request {@link URI}. This part of the {@link URI} is always the same
     * (it contains fixed values).
     */
    private static final String FIXED_PART = "&prompt=none&scope=openid&response_type=id_token&response_mode=form_post";

    private final LtiService ltiService;

    private final Validator validator;

    @Autowired
    public LtiController(final LtiService ltiService, final Validator validator) {
        this.ltiService = ltiService;
        this.validator = validator;
    }

    // TODO: cleanup code.

    @GetMapping("init-login")
    @ResponseStatus(code = HttpStatus.TEMPORARY_REDIRECT)
    public String login(@RequestParam("iss") final String issuer,
                        @RequestParam("login_hint") final String loginHint,
                        @RequestParam("target_link_uri") final String targetLinkUri,
                        @RequestParam(value = "lti_message_hint", required = false) final String ltiMessageHint,
                        @RequestParam(value = "lti_deployment_id", required = false) final String deploymentId,
                        @RequestParam(value = "client_id", required = false) final String clientId) {
        final var request = new LoginInitiationRequest(issuer, loginHint, targetLinkUri, ltiMessageHint, deploymentId, clientId);
        final var authenticationRequest = ltiService.loginInitiation(request); // TODO: handle errors
        final var uri = buildAuthenticationRequestUri(authenticationRequest);

        return "redirect:" + uri.toString();
    }

    @PostMapping(value = "create-exam", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String examSelection(final AuthenticationResponseForm form, final Model model) {
        return handleLtiMessage(form, model, this::examSelection);
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
        final var request = new ExamSelectedRequest(form.getExamId(), form.getState());
        final var response = ltiService.examSelected(request);
        model.addAttribute("endpoint", response.getEndpoint());
        model.addAttribute("jwt", response.getJwt());
        model.addAttribute("examData", response.getExamData());
        return "exam-selected";
    }

    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String LOGIN_HINT_PARAM = "login_hint";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String NONCE_PARAM = "nonce";

    private static final String LTI_MESSAGE_HINT_PARAM = "lti_message_hint";
    private static final String STATE_PARAM = "state";

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
     * Handles an Lti authentication response message.
     *
     * @param form              The {@link AuthenticationResponseForm} carrying the received data (i.e state and ID token).
     * @param model             The {@link Model} used to configure the result.
     * @param ltiMessageHandler An {@link LtiMessageHandler} with the action to be performed in case the
     *                          {@link AuthenticationResponseForm} carries valid data.
     * @return The result of the {@link LtiMessageHandler} if the {@link AuthenticationResponseForm} carries valid data,
     * or the {@code "authentication-response-error"} view otherwise.
     */
    private String handleLtiMessage(
            final AuthenticationResponseForm form,
            final Model model,
            final LtiMessageHandler ltiMessageHandler) {
        if (!validator.validate(form).isEmpty()) {
            model.addAttribute("authenticationResponseForm", form);
            return "authentication-response-error";
        }

        return ltiMessageHandler.handle(form.toAuthenticationResponse());
    }

    /**
     * Handles the exam selection request.
     *
     * @param response The {@link AuthenticationResponse} representing an exam selection deep linking request.
     * @return A {@link String} representing the view to be shown.
     */
    private String examSelection(final AuthenticationResponse response) {
        final var examSelectionResponse = ltiService.examSelection(response); // TODO: handle errors
        return "redirect:exam-selection?state=" + ltiService.examSelection(response).getState();
    }


    /**
     * A functional interface that defines a method to handle LTI messages.
     * This method will be called by {@link #handleLtiMessage(AuthenticationResponseForm, Model, LtiMessageHandler)}
     * if the {@link AuthenticationResponseForm} is valid.
     * This method should redirect to the corresponding endpoint if the message could be handled.
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
