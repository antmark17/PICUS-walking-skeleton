package it.picus.organizations.api;

import java.util.UUID;

/** Contratto minimale per verificare un'organizzazione OPT nota. */
public interface OrganizationLookup {
    boolean existsById(UUID organizationId);
}
