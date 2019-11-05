package ar.edu.itba.cep.lti_app.web.controller;

import ar.edu.itba.cep.lti.*;
import ar.edu.itba.cep.lti_app.web.dtos.AuthenticationResponseForm;
import ar.edu.itba.cep.lti_app.web.dtos.ExamSelectedForm;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.validation.Valid;

/**
 * LTI controller.
 */
@Controller
@RequestMapping("/exam-selection")
@AllArgsConstructor
public class LtiExamSelectionController {

    // ===========================================================================
    // Needed stuff
    // ===========================================================================

    /**
     * The {@link LtiExamSelectionService} to which the LTI messages are routed.
     */
    private final LtiExamSelectionService ltiExamSelectionService;
    /**
     * The {@link LtiAuthenticationResponseControllerHelper} to which
     */
    private final LtiAuthenticationResponseControllerHelper ltiAuthenticationResponseControllerHelper;


    // ================================================================================================================
    // Endpoints
    // ================================================================================================================


    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(code = HttpStatus.SEE_OTHER)
    public String examSelection(final AuthenticationResponseForm form) {
        return ltiAuthenticationResponseControllerHelper.handleLtiMessage(form, this::examSelection);
    }

    @GetMapping
    public String examSelection(
            @RequestParam(value = "state") final String state,
            @RequestParam(value = "examId", required = false) final Long examId,
            final Model model) {
        final var examSelectedForm = new ExamSelectedForm(examId, state);
        model.addAttribute("examSelectionForm", examSelectedForm);

        return "exam-selection";
    }


    @PostMapping(value = "/selected", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String examSelected(
            @Valid @ModelAttribute("examSelectionForm") final ExamSelectedForm form,
            final BindingResult result,
            final Model model) {
        if (result.hasErrors()) {
            return "exam-selection";
        }

        final var url = MvcUriComponentsBuilder.fromController(LtiExamTakingController.class).build().toString();


        final var request = new ExamSelectedRequest(form.getExamId(), form.getState(), url, null, null);
        final var response = ltiExamSelectionService.examSelected(request);
        model.addAttribute("examId", form.getExamId());
        if (response instanceof NonExistingExamSelectedResponse) {
            model.addAttribute("cause", "non-existing");
            return "exam-with-error";
        }
        if (response instanceof NotUpcomingExamSelectedResponse) {
            model.addAttribute("cause", "not-upcoming");
            return "exam-with-error";
        }
        final var existingExamSelectedResponse = (ExistingExamSelectedResponse) response;
        model.addAttribute("endpoint", existingExamSelectedResponse.getEndpoint());
        model.addAttribute("jwt", existingExamSelectedResponse.getJwt());
        model.addAttribute("examData", existingExamSelectedResponse.getExamData());
        return "exam-selected";
    }


    // ================================================================================================================
    // Helpers
    // ================================================================================================================

    /**
     * Handles the exam selection request.
     *
     * @param response The {@link AuthenticationResponse} representing an exam selection deep linking request.
     * @return A {@link String} representing the view to be shown.
     */
    private String examSelection(final AuthenticationResponse response) {
        return "exam-selection?state=" + ltiExamSelectionService.examSelection(response).getState();
    }
}
