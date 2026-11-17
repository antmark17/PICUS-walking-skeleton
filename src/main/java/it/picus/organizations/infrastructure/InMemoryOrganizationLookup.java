
package it.picus.organizations.infrastructure;

import it.picus.organizations.api.OrganizationLookup;
import it.picus.organizations.domain.DemoOrganizationIds;
import it.picus.organizations.domain.Organization;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Implementazione minimale di OrganizationLookup.
 * Gestisce soltanto le organizzazioni sintetiche del MVP.
 *
 * Non implementa operazioni CRUD o accesso al database.
 */
@Component
public final class InMemoryOrganizationLookup
        implements OrganizationLookup {

    private final Map<UUID, Organization> organizations = Map.of(
            DemoOrganizationIds.OPT_A,
            new Organization(
                    DemoOrganizationIds.OPT_A,
                    "OPT A"
            ),

            DemoOrganizationIds.OPT_B,
            new Organization(
                    DemoOrganizationIds.OPT_B,
                    "OPT B"
            )
    );

    @Override
    public boolean existsById(UUID organizationId) {
        return organizationId != null
                && organizations.containsKey(organizationId);
    }
}
