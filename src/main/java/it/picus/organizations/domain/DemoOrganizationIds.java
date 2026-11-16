
package it.picus.organizations.domain;

import java.util.UUID;

/**
 * Identificativi stabili delle organizzazioni
 * sintetiche del Walking Skeleton PICUS.
 */
public final class DemoOrganizationIds {

    public static final UUID OPT_A =
            UUID.fromString("88000000-0000-4000-8000-00000000000a");

    public static final UUID OPT_B =
            UUID.fromString("88000000-0000-4000-8000-00000000000b");

    private DemoOrganizationIds() {
        throw new AssertionError("Classe di sole costanti");
    }
}
