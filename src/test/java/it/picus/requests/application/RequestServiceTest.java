package it.picus.requests.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import it.picus.identity.api.ActorRole;
import it.picus.identity.api.AuthenticatedActor;
import it.picus.identity.api.CurrentActorProvider;
import it.picus.organizations.api.OrganizationLookup;
import it.picus.requests.api.RequestView;
import it.picus.requests.domain.InvalidRequestStateTransitionException;
import it.picus.requests.domain.RequestStatus;
import it.picus.requests.domain.ServiceRequest;
import it.picus.requests.domain.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
    @Mock ServiceRequestRepository repository;
    @Mock CurrentActorProvider currentActorProvider;
    @Mock OrganizationLookup organizationLookup;

    private RequestService service;

    @BeforeEach
    void setUp() {
        service = new RequestService(repository, currentActorProvider, organizationLookup);
    }

    @Test
    void optSubmitsRequestForAuthenticatedOrganization() {
        UUID organizationId = UUID.randomUUID();
        givenOpt(organizationId);
        when(organizationLookup.existsById(organizationId)).thenReturn(true);
        when(repository.save(any(ServiceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UUID id = service.submitRequest("Accompagnamento al lavoro");

        ArgumentCaptor<ServiceRequest> requestCaptor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(repository).save(requestCaptor.capture());
        ServiceRequest saved = requestCaptor.getValue();
        assertAll(
                () -> assertEquals(saved.getId(), id),
                () -> assertEquals(organizationId, saved.getOrganizationId()),
                () -> assertEquals("Accompagnamento al lavoro", saved.getMisuraRichiesta()),
                () -> assertEquals(RequestStatus.DA_VALUTARE, saved.getStatus()));
    }

    @Test
    void submitRejectsUnknownOrganizationWithoutSaving() {
        UUID organizationId = UUID.randomUUID();
        givenOpt(organizationId);
        when(organizationLookup.existsById(organizationId)).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.submitRequest("Accompagnamento al lavoro"));

        verify(repository, never()).save(any());
    }

    @Test
    void bancoCannotSubmitOptRequest() {
        givenBanco();

        assertThrows(
                AccessDeniedException.class,
                () -> service.submitRequest("Accompagnamento al lavoro"));

        verifyNoInteractions(organizationLookup, repository);
    }

    @Test
    void listMineQueriesOnlyAuthenticatedOrganization() {
        UUID organizationId = UUID.randomUUID();
        ServiceRequest request = requestFor(organizationId, RequestStatus.DA_VALUTARE);
        givenOpt(organizationId);
        when(repository.findByOrganizationId(organizationId)).thenReturn(List.of(request));

        List<RequestView> result = service.listMine();

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(request.getId(), result.getFirst().id()),
                () -> assertEquals(organizationId, result.getFirst().organizationId()));
        verify(repository).findByOrganizationId(organizationId);
        verify(repository, never()).findAll();
    }

    @Test
    void getMineUsesOwnershipQuery() {
        UUID organizationId = UUID.randomUUID();
        ServiceRequest request = requestFor(organizationId, RequestStatus.DA_VALUTARE);
        givenOpt(organizationId);
        when(repository.findByIdAndOrganizationId(request.getId(), organizationId))
                .thenReturn(Optional.of(request));

        RequestView result = service.getMine(request.getId());

        assertEquals(request.getId(), result.id());
        verify(repository).findByIdAndOrganizationId(request.getId(), organizationId);
        verify(repository, never()).findById(any());
    }

    @Test
    void getMineHidesMissingOrForeignRequestAsNotFound() {
        UUID organizationId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        givenOpt(organizationId);
        when(repository.findByIdAndOrganizationId(requestId, organizationId))
                .thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class, () -> service.getMine(requestId));

        verify(repository, never()).findById(any());
    }

    @Test
    void bancoListsAndReadsAllRequests() {
        ServiceRequest request = requestFor(UUID.randomUUID(), RequestStatus.DA_VALUTARE);
        givenBanco();
        when(repository.findAll()).thenReturn(List.of(request));
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        List<RequestView> all = service.listForBanco();
        RequestView single = service.getForBanco(request.getId());

        assertAll(
                () -> assertEquals(1, all.size()),
                () -> assertEquals(request.getId(), all.getFirst().id()),
                () -> assertEquals(request.getId(), single.id()));
    }

    @Test
    void optCannotUseBancoOperations() {
        givenOpt(UUID.randomUUID());

        assertAll(
                () -> assertThrows(AccessDeniedException.class, service::listForBanco),
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> service.getForBanco(UUID.randomUUID())),
                () -> assertThrows(
                        AccessDeniedException.class,
                        () -> service.startProcessing(UUID.randomUUID())));

        verifyNoInteractions(repository);
    }

    @Test
    void bancoStartsProcessingAndSavesRequest() {
        ServiceRequest request = requestFor(UUID.randomUUID(), RequestStatus.DA_VALUTARE);
        givenBanco();
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        service.startProcessing(request.getId());

        assertEquals(RequestStatus.IN_ELABORAZIONE, request.getStatus());
        verify(repository).save(request);
    }

    @Test
    void invalidTransitionDoesNotSaveRequest() {
        ServiceRequest request = requestFor(UUID.randomUUID(), RequestStatus.IN_ELABORAZIONE);
        givenBanco();
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(
                InvalidRequestStateTransitionException.class,
                () -> service.startProcessing(request.getId()));

        verify(repository, never()).save(any());
    }

    private void givenOpt(UUID organizationId) {
        when(currentActorProvider.currentActor()).thenReturn(new AuthenticatedActor(
                "opt@example.test",
                ActorRole.OPT,
                Optional.of(organizationId)));
    }

    private void givenBanco() {
        when(currentActorProvider.currentActor()).thenReturn(new AuthenticatedActor(
                "banco@example.test",
                ActorRole.BANCO,
                Optional.empty()));
    }

    private static ServiceRequest requestFor(UUID organizationId, RequestStatus status) {
        return new ServiceRequest(
                UUID.randomUUID(),
                organizationId,
                "Accompagnamento al lavoro",
                status,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"));
    }
}
