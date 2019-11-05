package ar.edu.itba.cep.lti_app.web.controller;

import ar.edu.itba.cep.lti_app.web.exceptions.AuthenticationResponseWithMissingParamsException;
import ar.edu.itba.cep.lti_app.web.exceptions.LoginInitiationRequestWithMissingParamsException;
import com.bellotapps.webapps_commons.exceptions.ExternalServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A controller to handle exceptions.
 */
@ControllerAdvice
public class ExceptionController {

    /**
     * Handles the {@link ExternalServiceException}, displaying the {@code external-service-exception} view.
     *
     * @return The view.
     */
    @ExceptionHandler(value = {
            ExternalServiceException.class
    })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleExternalServiceException(final ExternalServiceException e) {
        return "external-service-exception";
    }

    /**
     * Handles the {@link LoginInitiationRequestWithMissingParamsException},
     * displaying the {@code login-initiation-error} view.
     *
     * @param e     The {@link LoginInitiationRequestWithMissingParamsException} to be handled.
     * @param model The {@link Model} used to bind the {@link ar.edu.itba.cep.lti_app.web.dtos.LoginInitiationRequestDto}
     *              with errors with the UI.
     * @return The view.
     */
    @ExceptionHandler(value = {
            LoginInitiationRequestWithMissingParamsException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleLoginInitiationRequestWithMissingParamsException(
            final LoginInitiationRequestWithMissingParamsException e,
            final Model model) {
        model.addAttribute("loginInitiationRequestDto", e.getDto());
        return "login-initiation-error";
    }


    /**
     * Handles the {@link AuthenticationResponseWithMissingParamsException},
     * displaying the {@code authentication-response-error} view.
     *
     * @param e     The {@link AuthenticationResponseWithMissingParamsException} to be handled.
     * @param model The {@link Model} used to bind the {@link ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm}
     *              with errors with the UI.
     * @return The view.
     */
    @ExceptionHandler(value = {
            AuthenticationResponseWithMissingParamsException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleAuthenticationResponseWithMissingParamsException(
            final AuthenticationResponseWithMissingParamsException e,
            final Model model) {
        model.addAttribute("authenticationResponseForm", e.getForm());
        return "authentication-response-error";
    }
}
