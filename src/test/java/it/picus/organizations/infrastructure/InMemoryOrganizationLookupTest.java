
package it.picus.organizations.infrastructure;

import it.picus.organizations.domain.DemoOrganizationIds;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOrganizationLookupTest {

    private final InMemoryOrganizationLookup lookup =
            new InMemoryOrganizationLookup();

    @Test
    void recognizesOnlyTheTwoKnownDemoOrganizations() {

        // OPT A deve esistere
        assertTrue(
                lookup.existsById(DemoOrganizationIds.OPT_A)
        );

        // OPT B deve esistere
        assertTrue(
                lookup.existsById(DemoOrganizationIds.OPT_B)
        );

        // Organizzazione sconosciuta
        assertFalse(
                lookup.existsById(
                        UUID.fromString(
                            "88000000-0000-4000-8000-00000000000c"
                        )
                )
        );

        // Identificativo nullo
        assertFalse(
                lookup.existsById(null)
        );
    }
}
