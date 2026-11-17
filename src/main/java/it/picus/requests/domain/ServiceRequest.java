package it.picus.requests.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Modello iniziale da completare nel Gruppo 2, senza dati identificativi degli assistiti. */
public class ServiceRequest {
    private final UUID id;
    private final UUID organizationId;
    private final String misuraRichiesta;
    private RequestStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public static ServiceRequest create(UUID organizationId, String misuraRichiesta) {
        Instant now = Instant.now();
        return new ServiceRequest(
                UUID.randomUUID(),
                organizationId,
                misuraRichiesta,
                RequestStatus.DA_VALUTARE,
                now,
                now);
    }

    public ServiceRequest(UUID id, UUID organizationId, String misuraRichiesta,
                          RequestStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId");
        this.misuraRichiesta = requireValidMisura(misuraRichiesta);
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getMisuraRichiesta() { return misuraRichiesta; }
    public RequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void startProcessing() {
        if (status != RequestStatus.DA_VALUTARE) {
            throw new InvalidRequestStateTransitionException(
                    status,
                    RequestStatus.IN_ELABORAZIONE);
        }

        status = RequestStatus.IN_ELABORAZIONE;
        updatedAt = Instant.now();
    }

    private static String requireValidMisura(String misuraRichiesta) {
        if (misuraRichiesta == null || misuraRichiesta.isBlank()) {
            throw new IllegalArgumentException("misuraRichiesta non puo essere vuota");
        }
        return misuraRichiesta;
    }
}
