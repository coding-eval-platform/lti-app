package ar.edu.itba.cep.lti_app.service;

import ar.edu.itba.cep.lti.*;
import ar.edu.itba.cep.lti.constants.Paths;
import ar.edu.itba.cep.lti.dtos.*;
import com.bellotapps.webapps_commons.exceptions.ExternalServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

/**
 * Concrete implementation of {@link LtiService} using a {@link RestTemplate} to communicate with the real services.
 */
@Service
class RestTemplateLtiService implements LtiService {

    /**
     * The {@link RestTemplate} used to perform requests.
     */
    private final RestTemplate restTemplate;
    /**
     * {@link URI} of the login initiation endpoint.
     */
    private final URI loginInitUri;
    /**
     * {@link URI} of the exam selection endpoint.
     */
    private final URI examSelectionUri;
    /**
     * {@link URI} of the exam selected endpoint.
     */
    private final URI examSelectedUri;
    /**
     * {@link URI} of the exam taking endpoint.
     */
    private final URI examTakingUri;


    /**
     * Constructor.
     *
     * @param restTemplate The {@link RestTemplate} used to perform requests.
     * @param properties   The {@link LtiServiceConfig.Properties} needed to configure this service.
     */
    public RestTemplateLtiService(final RestTemplate restTemplate, final LtiServiceConfig.Properties properties) {
        this.restTemplate = restTemplate;
        this.loginInitUri = buildWithSimplePath(properties.getBaseUrl(), Paths.LOGIN_INITIATION_PATH);
        this.examSelectionUri = buildWithSimplePath(properties.getBaseUrl(), Paths.EXAM_SELECTION_PATH);
        this.examSelectedUri = buildWithSimplePath(properties.getBaseUrl(), Paths.EXAM_SELECTED_PATH);
        this.examTakingUri = buildWithSimplePath(properties.getBaseUrl(), Paths.EXAM_TAKING_PATH);
    }


    @Override
    public AuthenticationRequest loginInitiation(final LoginInitiationRequest loginInitiationRequest)
            throws ExternalServiceException {
        return postForObject(
                loginInitiationRequest,
                AuthenticationRequestDto.class,
                loginInitUri,
                LoginInitiationRequestDto::fromModel,
                AuthenticationRequestDto::toModel
        );
    }

    @Override
    public ExamSelectionResponse examSelection(final AuthenticationResponse authenticationResponse)
            throws ExternalServiceException {
        return postForObject(
                authenticationResponse,
                ExamSelectionResponseDto.class,
                examSelectionUri,
                AuthenticationResponseDto::fromModel,
                ExamSelectionResponseDto::toModel
        );
    }

    @Override
    public ExamSelectedResponse examSelected(final ExamSelectedRequest examSelectedRequest)
            throws ExternalServiceException {
        return postForObject(
                examSelectedRequest,
                ExamSelectedResponseDto.class,
                examSelectedUri,
                ExamSelectedRequestDto::fromModel,
                ExamSelectedResponseDto::toModel
        );
    }

    @Override
    public ExamTakingResponse takeExam(final AuthenticationResponse authenticationResponse)
            throws ExternalServiceException {
        return postForObject(
                authenticationResponse,
                ExamTakingResponseDto.class,
                examTakingUri,
                AuthenticationResponseDto::fromModel,
                ExamTakingResponseDto::toModel
        );
    }

    /**
     * Performs a post.
     *
     * @param model            The model to post.
     * @param dtoClass         The class of the response DTO.
     * @param uri              The {@link URI} to where to post.
     * @param modelToDtoMapper A {@link ModelToDtoMapper} to map the given {@code model} into its DTO version.
     * @param dtoToModelMapper A {@link DtoToModelMapper} to map the response DTO into its model version.
     * @param <REQM>           The request model's concrete type
     * @param <REQD>           The request DTO's concrete type
     * @param <RESPM>          The response model's concrete type
     * @param <RESPD>The       response DTO's concrete type
     * @return The returned response.
     * @throws ExternalServiceException If there is any issue when communicating performing the REST request.
     */
    private <REQM, REQD, RESPM, RESPD> RESPM postForObject(
            final REQM model,
            final Class<RESPD> dtoClass,
            final URI uri,
            final ModelToDtoMapper<REQM, REQD> modelToDtoMapper,
            final DtoToModelMapper<RESPM, RESPD> dtoToModelMapper) throws ExternalServiceException {
        final var entity = new HttpEntity<>(modelToDtoMapper.map(model));
        try {
            return Optional.ofNullable(restTemplate.postForObject(uri, entity, dtoClass))
                    .map(dtoToModelMapper::map)
                    .orElseThrow();
        } catch (final Throwable e) {
            throw new ExternalServiceException("lti-service", "Could not communicate with LTI service", e);
        }
    }


    /**
     * Builds a {@link URI} from the given {@code baseUrl}, appending the given {@code path}.
     *
     * @param baseUrl The base url
     * @param path    The path to be appended.
     * @return The created {@link URI}.
     */
    private static URI buildWithSimplePath(final String baseUrl, final String path) {
        return UriComponentsBuilder.fromUriString(baseUrl).path(path).build().toUri();
    }


    /**
     * Function interface used to map a model class into its DTO version.
     *
     * @param <M> The model's concrete type
     * @param <D> The DTO's concrete type
     */
    @FunctionalInterface
    private interface ModelToDtoMapper<M, D> {
        D map(final M model);
    }

    /**
     * Function interface used to map a DTO class into its model version.
     *
     * @param <M> The model's concrete type
     * @param <D> The DTO's concrete type
     */
    @FunctionalInterface
    private interface DtoToModelMapper<M, D> {
        M map(final D model);
    }
}
