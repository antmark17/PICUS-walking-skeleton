package it.picus.identity.api;

/** Contratto fornito dal Gruppo 1 e consumato da Requests (Gruppo 2). */
public interface CurrentActorProvider {
    AuthenticatedActor currentActor();
}
