
package it.picus.identity.infrastructure;

import it.picus.identity.api.ActorRole;
import it.picus.identity.api.AuthenticatedActor;
import it.picus.identity.api.CurrentActorProvider;
import it.picus.organizations.api.OrganizationLookup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(MethodSecurityIntegrationTest.TestBeans.class)
class MethodSecurityIntegrationTest {

    @Autowired
    private BancoAction bancoAction;

    @Autowired
    private CurrentActorProvider currentActorProvider;

    @Autowired
    private OrganizationLookup organizationLookup;

    @Test
    @WithUserDetails("opt_a")
    void optCannotExecuteBancoService() {
        assertThrows(
                AccessDeniedException.class,
                bancoAction::execute
        );
    }

    @Test
    @WithUserDetails("banco")
    void bancoCanExecuteBancoService() {
        assertEquals("OK", bancoAction.execute());
    }

    @Test
    @WithUserDetails("opt_a")
    void g2ContractsAreInjectable() {
        AuthenticatedActor actor =
                currentActorProvider.currentActor();

        assertEquals(ActorRole.OPT, actor.role());

        assertTrue(actor.organizationId().isPresent());

        assertTrue(
                organizationLookup.existsById(
                        actor.organizationId().orElseThrow()
                )
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestBeans {

        @Bean
        BancoAction bancoAction() {
            return new BancoAction();
        }
    }

    public static class BancoAction {

        @PreAuthorize("hasRole('BANCO')")
        public String execute() {
            return "OK";
        }
    }
}
