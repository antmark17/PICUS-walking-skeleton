
package it.picus.organizations.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Modello minimale di un'organizzazione PICUS.
 * Non contiene dati relativi alle persone assistite.
 */
public record Organization(
        UUID id,
        String name
) {

    public Organization {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                "Il nome dell'organizzazione non puo' essere vuoto"
            );
        }
    }
}
