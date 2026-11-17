package it.picus.requests.domain;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceRequestTest {

    @Test
    void createsRequestInInitialState() {
        UUID organizationId = UUID.randomUUID();

        ServiceRequest request = ServiceRequest.create(
                organizationId,
                "Accompagnamento al lavoro");

        assertAll(
                () -> assertNotNull(request.getId()),
                () -> assertEquals(organizationId, request.getOrganizationId()),
                () -> assertEquals("Accompagnamento al lavoro", request.getMisuraRichiesta()),
                () -> assertEquals(RequestStatus.DA_VALUTARE, request.getStatus()),
                () -> assertNotNull(request.getCreatedAt()),
                () -> assertEquals(request.getCreatedAt(), request.getUpdatedAt()));
    }

    @Test
    void generatesDifferentIdsForDifferentRequests() {
        UUID organizationId = UUID.randomUUID();

        ServiceRequest first = ServiceRequest.create(organizationId, "Prima misura");
        ServiceRequest second = ServiceRequest.create(organizationId, "Seconda misura");

        assertNotEquals(first.getId(), second.getId());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void rejectsMissingOrBlankMisura(String misuraRichiesta) {
        UUID organizationId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> ServiceRequest.create(organizationId, misuraRichiesta));
    }

    @Test
    void rejectsMissingOrganization() {
        assertThrows(
                NullPointerException.class,
                () -> ServiceRequest.create(null, "Accompagnamento al lavoro"));
    }

    @Test
    void startsProcessingFromInitialState() {
        ServiceRequest request = new ServiceRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Accompagnamento al lavoro",
                RequestStatus.DA_VALUTARE,
                Instant.EPOCH,
                Instant.EPOCH);

        request.startProcessing();

        assertAll(
                () -> assertEquals(RequestStatus.IN_ELABORAZIONE, request.getStatus()),
                () -> assertTrue(request.getUpdatedAt().isAfter(Instant.EPOCH)));
    }

    @Test
    void rejectsStartingProcessingTwiceWithoutChangingRequest() {
        ServiceRequest request = ServiceRequest.create(
                UUID.randomUUID(),
                "Accompagnamento al lavoro");
        request.startProcessing();
        Instant updatedAtAfterValidTransition = request.getUpdatedAt();

        InvalidRequestStateTransitionException exception = assertThrows(
                InvalidRequestStateTransitionException.class,
                request::startProcessing);

        assertAll(
                () -> assertEquals(RequestStatus.IN_ELABORAZIONE, request.getStatus()),
                () -> assertSame(updatedAtAfterValidTransition, request.getUpdatedAt()),
                () -> assertTrue(exception.getMessage().contains("IN_ELABORAZIONE")));
    }
}
