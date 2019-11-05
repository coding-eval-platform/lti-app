package ar.edu.itba.cep.lti_app.web.controller;

import ar.edu.itba.cep.lti.AuthenticationResponse;
import ar.edu.itba.cep.lti.LtiExamTakingService;
import ar.edu.itba.cep.lti_app.Application;
import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * LTI controller.
 */
@Controller
@RequestMapping("/exam-taking")
@AllArgsConstructor
public class LtiExamTakingController implements InitializingBean {

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
     * The {@link LtiExamTakingService} to which the LTI messages are routed.
     */
    private final LtiExamTakingService ltiExamTakingService;
    /**
     * The {@link LtiAuthenticationResponseControllerHelper} to which
     */
    private final LtiAuthenticationResponseControllerHelper ltiAuthenticationResponseControllerHelper;
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

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(code = HttpStatus.SEE_OTHER)
    public String takeExam(final AuthenticationResponseForm form) {
        return ltiAuthenticationResponseControllerHelper.handleLtiMessage(form, this::takeExam);
    }

    @GetMapping
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
     * Handles the exam take request.
     *
     * @param response The {@link AuthenticationResponse} representing an exam selection resource link launch request.
     * @return A {@link String} representing the view to be shown.
     */
    private String takeExam(final AuthenticationResponse response) {
        final var examTakingResponse = ltiExamTakingService.takeExam(response);
        return "exam-taking"
                + "?examId=" + examTakingResponse.getExamId()
                + "&tokenId=" + examTakingResponse.getTokenId()
                + "&accessToken=" + examTakingResponse.getAccessToken()
                + "&refreshToken=" + examTakingResponse.getRefreshToken()
                + "&returnUrl=" + URLEncoder.encode(examTakingResponse.getReturnUrl(), StandardCharsets.UTF_8)
                ;

    }
}
