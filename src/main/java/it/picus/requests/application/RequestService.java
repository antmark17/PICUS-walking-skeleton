package it.picus.requests.application;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import it.picus.identity.api.ActorRole;
import it.picus.identity.api.AuthenticatedActor;
import it.picus.identity.api.CurrentActorProvider;
import it.picus.organizations.api.OrganizationLookup;
import it.picus.requests.api.RequestUseCases;
import it.picus.requests.api.RequestView;
import it.picus.requests.domain.ServiceRequest;
import it.picus.requests.domain.ServiceRequestRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RequestService implements RequestUseCases {
    private final ServiceRequestRepository repository;
    private final CurrentActorProvider currentActorProvider;
    private final OrganizationLookup organizationLookup;

    public RequestService(
            ServiceRequestRepository repository,
            CurrentActorProvider currentActorProvider,
            OrganizationLookup organizationLookup) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.currentActorProvider = Objects.requireNonNull(currentActorProvider, "currentActorProvider");
        this.organizationLookup = Objects.requireNonNull(organizationLookup, "organizationLookup");
    }

    @Override
    @Transactional
    public UUID submitRequest(String misuraRichiesta) {
        UUID organizationId = currentOptOrganizationId();
        if (!organizationLookup.existsById(organizationId)) {
            throw new AccessDeniedException("Organizzazione OPT non riconosciuta");
        }

        ServiceRequest request = ServiceRequest.create(organizationId, misuraRichiesta);
        return repository.save(request).getId();
    }

    @Override
    public List<RequestView> listMine() {
        UUID organizationId = currentOptOrganizationId();
        return repository.findByOrganizationId(organizationId).stream()
                .map(RequestService::toView)
                .toList();
    }

    @Override
    public RequestView getMine(UUID id) {
        Objects.requireNonNull(id, "id");
        UUID organizationId = currentOptOrganizationId();
        return repository.findByIdAndOrganizationId(id, organizationId)
                .map(RequestService::toView)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }

    @Override
    public List<RequestView> listForBanco() {
        requireBanco();
        return repository.findAll().stream()
                .map(RequestService::toView)
                .toList();
    }

    @Override
    public RequestView getForBanco(UUID id) {
        Objects.requireNonNull(id, "id");
        requireBanco();
        return repository.findById(id)
                .map(RequestService::toView)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }

    @Override
    @Transactional
    public void startProcessing(UUID id) {
        Objects.requireNonNull(id, "id");
        requireBanco();
        ServiceRequest request = repository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
        request.startProcessing();
        repository.save(request);
    }

    private UUID currentOptOrganizationId() {
        AuthenticatedActor actor = currentActor();
        if (actor.role() != ActorRole.OPT) {
            throw new AccessDeniedException("Operazione consentita soltanto alle OPT");
        }
        return actor.organizationId()
                .orElseThrow(() -> new AccessDeniedException("Identita OPT senza organizzazione"));
    }

    private void requireBanco() {
        if (currentActor().role() != ActorRole.BANCO) {
            throw new AccessDeniedException("Operazione consentita soltanto al Banco");
        }
    }

    private AuthenticatedActor currentActor() {
        return Objects.requireNonNull(currentActorProvider.currentActor(), "currentActor");
    }

    private static RequestView toView(ServiceRequest request) {
        return new RequestView(
                request.getId(),
                request.getOrganizationId(),
                request.getMisuraRichiesta(),
                request.getStatus(),
                request.getCreatedAt());
    }
}
