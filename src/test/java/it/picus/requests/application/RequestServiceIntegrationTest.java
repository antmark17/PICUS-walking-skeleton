package it.picus.requests.application;

import java.time.Instant;
import java.util.UUID;

import it.picus.requests.api.RequestUseCases;
import it.picus.requests.domain.InvalidRequestStateTransitionException;
import it.picus.requests.domain.RequestStatus;
import it.picus.requests.domain.ServiceRequest;
import it.picus.requests.domain.ServiceRequestRepository;
import it.picus.testsupport.RequestsExternalDependenciesTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(RequestsExternalDependenciesTestConfiguration.class)
class RequestServiceIntegrationTest {
    @Autowired RequestUseCases useCases;
    @Autowired ServiceRequestRepository repository;

    @Test
    void bancoTransitionIsPersistedAndInvalidRetryLeavesRecordUnchanged() {
        ServiceRequest request = repository.save(ServiceRequest.create(
                UUID.randomUUID(),
                "Accompagnamento al lavoro"));

        useCases.startProcessing(request.getId());

        ServiceRequest afterValidTransition = repository.findById(request.getId()).orElseThrow();
        Instant updatedAtAfterValidTransition = afterValidTransition.getUpdatedAt();
        assertEquals(RequestStatus.IN_ELABORAZIONE, afterValidTransition.getStatus());

        assertThrows(
                InvalidRequestStateTransitionException.class,
                () -> useCases.startProcessing(request.getId()));

        ServiceRequest afterInvalidRetry = repository.findById(request.getId()).orElseThrow();
        assertAll(
                () -> assertEquals(RequestStatus.IN_ELABORAZIONE, afterInvalidRetry.getStatus()),
                () -> assertEquals(
                        updatedAtAfterValidTransition,
                        afterInvalidRetry.getUpdatedAt()));
    }
}
