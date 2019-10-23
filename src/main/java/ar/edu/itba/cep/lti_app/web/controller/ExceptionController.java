package ar.edu.itba.cep.lti_app.web.controller;

import com.bellotapps.webapps_commons.exceptions.ExternalServiceException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    public String handleExternalServiceException() {
        return "external-service-exception";
    }
}
