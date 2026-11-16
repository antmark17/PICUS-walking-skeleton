
package it.picus.identity.infrastructure;

import it.picus.identity.api.AuthenticatedActor;
import it.picus.identity.api.CurrentActorProvider;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public final class SpringSecurityActorProvider
        implements CurrentActorProvider {

    @Override
    public AuthenticatedActor currentActor() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof PicusUserPrincipal principal)) {
            throw new AccessDeniedException(
                    "Nessun attore PICUS autenticato"
            );
        }

        return new AuthenticatedActor(
                principal.username(),
                principal.role(),
                Optional.ofNullable(principal.organizationId())
        );
    }
}
