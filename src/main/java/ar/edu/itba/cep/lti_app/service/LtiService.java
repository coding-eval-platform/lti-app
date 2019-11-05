package ar.edu.itba.cep.lti_app.service;

import ar.edu.itba.cep.lti.LtiExamSelectionService;
import ar.edu.itba.cep.lti.LtiExamTakingService;
import ar.edu.itba.cep.lti.LtiLoginService;

/**
 * Convenient interface that centralizes all of the LTI services defined in the commons library
 * that are used by this application.
 */
interface LtiService extends LtiLoginService, LtiExamSelectionService, LtiExamTakingService {
}
