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

    public ServiceRequest(UUID id, UUID organizationId, String misuraRichiesta,
                          RequestStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.misuraRichiesta = Objects.requireNonNull(misuraRichiesta);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getMisuraRichiesta() { return misuraRichiesta; }
    public RequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Gruppo 2: aggiungere factory, validazione e regola di transizione.
}
