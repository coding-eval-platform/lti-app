package ar.edu.itba.cep.lti_app.web.dtos;

import ar.edu.itba.cep.lti.LoginInitiationRequest;
import lombok.Value;

import javax.validation.constraints.NotNull;

/**
 * Data transfer object to receive Login Initiation forms/query params.
 *
 * @see ar.edu.itba.cep.lti_app.web.data_transfer.LoginInitiationRequestDtoArgumentResolver
 */
@Value
public class LoginInitiationRequestDto {

    /**
     * The issuing authority (identifies the LMS).
     */
    @NotNull
    private final String issuer;
    /**
     * Hint needed by the LMS.
     */
    @NotNull
    private final String loginHint;
    /**
     * The actual end-point that should be executed at the end of the authentication flow.
     */
    @NotNull
    private final String targetLinkUri;
    /**
     * An optional field used alongside the {@code loginHint} by the LMS
     * to carry information about the received LTI message.
     */
    private final String ltiMessageHint;
    /**
     * An optional field used to identify a specific LTI tool deployment.
     */
    private final String deploymentId;
    /**
     * An optional field used to identify a specific LTI tool deployment.
     */
    private final String clientId;

    /**
     * Converts {@code this} DTO into a {@link LoginInitiationRequest}.
     *
     * @return The {@link LoginInitiationRequest}.
     */
    public LoginInitiationRequest toModel() {
        return new LoginInitiationRequest(issuer, loginHint, targetLinkUri, ltiMessageHint, deploymentId, clientId);
    }
}
