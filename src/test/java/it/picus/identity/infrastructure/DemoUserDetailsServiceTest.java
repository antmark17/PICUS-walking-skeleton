
package it.picus.identity.infrastructure;

import it.picus.identity.api.ActorRole;
import it.picus.organizations.domain.DemoOrganizationIds;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class DemoUserDetailsServiceTest {

    private final PasswordEncoder encoder =
            new BCryptPasswordEncoder();

    private final DemoUserDetailsService service =
            new DemoUserDetailsService(
                    encoder,
                    "password-sintetica-per-test"
            );

    @Test
    void createsThreeActorsWithCorrectRolesAndOrganizationIds() {

        PicusUserPrincipal banco =
                (PicusUserPrincipal) service.loadUserByUsername("banco");

        PicusUserPrincipal optA =
                (PicusUserPrincipal) service.loadUserByUsername("opt_a");

        PicusUserPrincipal optB =
                (PicusUserPrincipal) service.loadUserByUsername("opt_b");

        // Verifica Banco
        assertEquals(ActorRole.BANCO, banco.role());
        assertNull(banco.organizationId());
        assertEquals(
                "ROLE_BANCO",
                banco.getAuthorities().iterator().next().getAuthority()
        );

        // Verifica OPT A
        assertEquals(ActorRole.OPT, optA.role());
        assertEquals(
                DemoOrganizationIds.OPT_A,
                optA.organizationId()
        );

        // Verifica OPT B
        assertEquals(ActorRole.OPT, optB.role());
        assertEquals(
                DemoOrganizationIds.OPT_B,
                optB.organizationId()
        );

        // Le organizzazioni devono essere differenti
        assertNotEquals(
                optA.organizationId(),
                optB.organizationId()
        );
    }

    @Test
    void storesOnlyBcryptEncodedPasswords() {

        for (String username :
                new String[]{"banco", "opt_a", "opt_b"}) {

            PicusUserPrincipal user =
                    (PicusUserPrincipal)
                            service.loadUserByUsername(username);

            assertNotEquals(
                    "password-sintetica-per-test",
                    user.getPassword()
            );

            assertTrue(
                    encoder.matches(
                            "password-sintetica-per-test",
                            user.getPassword()
                    )
            );
        }
    }

    @Test
    void refusesUnknownUser() {

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("sconosciuto")
        );

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null)
        );
    }

    @Test
    void rejectsInvalidRoleOrganizationCombination() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PicusUserPrincipal(
                        "opt",
                        "encoded",
                        ActorRole.OPT,
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PicusUserPrincipal(
                        "banco",
                        "encoded",
                        ActorRole.BANCO,
                        DemoOrganizationIds.OPT_A
                )
        );
    }
}
