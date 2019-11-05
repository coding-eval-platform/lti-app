package ar.edu.itba.cep.lti_app.web.controller;

import ar.edu.itba.cep.lti.AuthenticationResponse;
import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import ar.edu.itba.cep.lti_app.web.exceptions.AuthenticationResponseWithMissingParamsException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.Validator;

/**
 * Component in charge of providing a single point of control for handling LTI authentication responses.
 */
@Component
@AllArgsConstructor
class LtiAuthenticationResponseControllerHelper {

    /**
     * A {@link Validator} used to validate input data.
     */
    private final Validator validator;


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
    String handleLtiMessage(final AuthenticationResponseForm form, final LtiMessageHandler ltiMessageHandler) {
        if (!validator.validate(form).isEmpty()) {
            throw new AuthenticationResponseWithMissingParamsException(form);
        }
        return "redirect:" + ltiMessageHandler.handle(form.toAuthenticationResponse());
    }


    /**
     * A functional interface that defines a method to handle LTI messages.
     * This method will be called by {@link #handleLtiMessage(AuthenticationResponseForm, LtiMessageHandler)}
     * if the {@link AuthenticationResponseForm} is valid.
     * The result of this method will be appended to the "redirect:" prefix.
     */
    @FunctionalInterface
    interface LtiMessageHandler {

        /**
         * Handles the given {@code response}.
         *
         * @param response The {@link AuthenticationResponse} to be handled.
         * @return A {@link String} representing the redirection to be performed.
         */
        String handle(final AuthenticationResponse response);
    }
}
