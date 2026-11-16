package it.picus.requests.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import it.picus.identity.api.ActorRole;
import it.picus.identity.api.AuthenticatedActor;
import it.picus.identity.api.CurrentActorProvider;
import it.picus.requests.domain.RequestStatus;
import it.picus.requests.domain.ServiceRequest;
import it.picus.requests.infrastructure.JpaServiceRequestRepositoryAdapter;
import it.picus.requests.infrastructure.ServiceRequestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({JpaServiceRequestRepositoryAdapter.class, ServiceRequestMapper.class})
class RequestOwnershipIntegrationTest {
    @Autowired JpaServiceRequestRepositoryAdapter repository;

    @Test
    void optCannotReadOrModifyThirtyRequestsOwnedByAnotherOrganization() {
        UUID organizationA = UUID.randomUUID();
        UUID organizationB = UUID.randomUUID();
        List<UUID> organizationARequestIds = new ArrayList<>();
        for (int index = 0; index < 30; index++) {
            ServiceRequest request = ServiceRequest.create(
                    organizationA,
                    "Misura campione " + index);
            repository.save(request);
            organizationARequestIds.add(request.getId());
        }

        CurrentActorProvider actorProvider = () -> new AuthenticatedActor(
                "opt-b@example.test",
                ActorRole.OPT,
                Optional.of(organizationB));
        RequestService service = new RequestService(
                repository,
                actorProvider,
                organizationId -> true);

        for (UUID requestId : organizationARequestIds) {
            assertThrows(RequestNotFoundException.class, () -> service.getMine(requestId));
        }

        assertTrue(service.listMine().isEmpty());
        List<ServiceRequest> persisted = repository.findAll();
        assertEquals(30, persisted.size());
        assertTrue(persisted.stream().allMatch(request ->
                request.getOrganizationId().equals(organizationA)
                        && request.getStatus() == RequestStatus.DA_VALUTARE));
    }
}
