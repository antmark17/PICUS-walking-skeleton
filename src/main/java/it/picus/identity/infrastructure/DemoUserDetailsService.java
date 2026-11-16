
package it.picus.identity.infrastructure;

import it.picus.identity.api.ActorRole;
import it.picus.organizations.domain.DemoOrganizationIds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Utenti sintetici del Walking Skeleton PICUS.
 *
 * Gli account sono memorizzati in RAM.
 * La password viene letta dalla variabile
 * d'ambiente PICUS_DEMO_PASSWORD.
 *
 * Nessuna credenziale reale viene salvata nel codice.
 */
@Service
public final class DemoUserDetailsService
        implements UserDetailsService {

    private final Map<String, PicusUserPrincipal> users;

    public DemoUserDetailsService(
            PasswordEncoder encoder,
            @Value("${PICUS_DEMO_PASSWORD:}") String configuredPassword
    ) {
        Objects.requireNonNull(encoder, "encoder");

        String rawPassword =
                configuredPassword == null || configuredPassword.isBlank()
                        ? UUID.randomUUID().toString()
                        : configuredPassword;

        this.users = Map.of(
                "banco",
                new PicusUserPrincipal(
                        "banco",
                        encoder.encode(rawPassword),
                        ActorRole.BANCO,
                        null
                ),

                "opt_a",
                new PicusUserPrincipal(
                        "opt_a",
                        encoder.encode(rawPassword),
                        ActorRole.OPT,
                        DemoOrganizationIds.OPT_A
                ),

                "opt_b",
                new PicusUserPrincipal(
                        "opt_b",
                        encoder.encode(rawPassword),
                        ActorRole.OPT,
                        DemoOrganizationIds.OPT_B
                )
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if (username == null) {
            throw new UsernameNotFoundException(
                    "Utente non trovato"
            );
        }

        PicusUserPrincipal user = users.get(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "Utente non trovato"
            );
        }

        return user;
    }
}
