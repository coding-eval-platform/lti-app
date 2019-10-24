package ar.edu.itba.cep.lti_app.web.exceptions;

import ar.edu.itba.cep.lti_app.web.dtos.LoginInitiationRequestDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exception to be thrown when there are missing params in an {@link LoginInitiationRequestDto}.
 */
@Getter
@RequiredArgsConstructor
public class LoginInitiationRequestWithMissingParamsException extends RuntimeException {
    /**
     * The DTO that contains required arguments that are missing.
     */
    private final LoginInitiationRequestDto dto;
}
