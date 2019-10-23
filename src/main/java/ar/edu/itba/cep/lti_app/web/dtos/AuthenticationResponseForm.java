package ar.edu.itba.cep.lti_app.web.dtos;

import ar.edu.itba.cep.lti.AuthenticationResponse;
import lombok.Value;

import javax.validation.constraints.NotNull;

/**
 * Bean class to receive LTI Authentication Responses forms.
 *
 * @see ar.edu.itba.cep.lti_app.web.data_transfer.AuthenticationRequestFormArgumentResolver
 */
@Value
public final class AuthenticationResponseForm {

    @NotNull
    private final String idToken;
    @NotNull
    private final String state;

    /**
     * Builds an {@link AuthenticationResponse} using data taken from {@code this} instance.
     *
     * @return The created {@link AuthenticationResponse}.
     */
    public AuthenticationResponse toAuthenticationResponse() {
        return new AuthenticationResponse(idToken, state);
    }
}
