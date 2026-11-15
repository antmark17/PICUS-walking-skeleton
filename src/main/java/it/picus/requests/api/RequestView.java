package it.picus.requests.api;

import java.time.Instant;
import java.util.UUID;
import it.picus.requests.domain.RequestStatus;

/** Dati restituiti alle viste: nessun identificativo degli assistiti. */
public record RequestView(UUID id, UUID organizationId, String misuraRichiesta,
                          RequestStatus status, Instant createdAt) {}
