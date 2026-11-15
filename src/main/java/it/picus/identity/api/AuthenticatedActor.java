package it.picus.identity.api;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Identita fornita dal backend dopo l'autenticazione.
 * L'organizationId e' presente per OPT e assente per BANCO.
 * Non deriva mai da un campo inviato dal browser per autorizzare una richiesta.
 */
public record AuthenticatedActor(String username, ActorRole role, Optional<UUID> organizationId) {
    public AuthenticatedActor {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(organizationId, "organizationId");
        if (username.isBlank()) throw new IllegalArgumentException("username vuoto");
        if (role == ActorRole.OPT && organizationId.isEmpty()) {
            throw new IllegalArgumentException("L'identita OPT richiede organizationId");
        }
    }
}
