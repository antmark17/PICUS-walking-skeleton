
package it.picus.identity.infrastructure;

import it.picus.identity.api.ActorRole;
import it.picus.identity.api.AuthenticatedActor;
import it.picus.organizations.domain.DemoOrganizationIds;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpringSecurityActorProviderTest {

    private final SpringSecurityActorProvider provider =
            new SpringSecurityActorProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void optAHasCorrectOrganizationId() {
        authenticate(new PicusUserPrincipal(
                "opt_a",
                "encoded-test",
                ActorRole.OPT,
                DemoOrganizationIds.OPT_A
        ));

        AuthenticatedActor actor = provider.currentActor();

        assertEquals("opt_a", actor.username());
        assertEquals(ActorRole.OPT, actor.role());
        assertEquals(
                Optional.of(DemoOrganizationIds.OPT_A),
                actor.organizationId()
        );
    }

    @Test
    void bancoHasNoOrganizationId() {
        authenticate(new PicusUserPrincipal(
                "banco",
                "encoded-test",
                ActorRole.BANCO,
                null
        ));

        AuthenticatedActor actor = provider.currentActor();

        assertEquals(ActorRole.BANCO, actor.role());
        assertTrue(actor.organizationId().isEmpty());
    }

    @Test
    void anonymousAccessIsDenied() {
        assertThrows(
                AccessDeniedException.class,
                provider::currentActor
        );
    }

    private void authenticate(PicusUserPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                )
        );
    }
}
