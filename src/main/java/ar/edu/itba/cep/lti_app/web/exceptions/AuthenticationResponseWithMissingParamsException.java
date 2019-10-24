package ar.edu.itba.cep.lti_app.web.exceptions;

import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exception to be thrown when there are missing params in an {@link AuthenticationResponseForm}.
 */
@Getter
@RequiredArgsConstructor
public class AuthenticationResponseWithMissingParamsException extends RuntimeException {
    /**
     * The DTO that contains required arguments that are missing.
     */
    private final AuthenticationResponseForm form;
}
