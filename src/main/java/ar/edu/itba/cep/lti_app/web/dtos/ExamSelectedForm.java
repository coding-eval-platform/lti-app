package ar.edu.itba.cep.lti_app.web.dtos;

import lombok.Value;

import javax.validation.constraints.NotNull;

/**
 * Bean class representing the exam selected form.
 */
@Value
public final class ExamSelectedForm {

    @NotNull
    private final Long examId;
    @NotNull
    private final String state;


    /**
     * Creates an {@link ExamSelectedForm} instance that contains only the {@code state}.
     *
     * @param state The state.
     * @return The created instance.
     */
    public static ExamSelectedForm justState(final String state) {
        return new ExamSelectedForm(null, state);
    }

}
