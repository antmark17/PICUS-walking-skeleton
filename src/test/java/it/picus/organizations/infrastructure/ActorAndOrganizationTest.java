
package it.picus.organizations.infrastructure;

import it.picus.identity.api.AuthenticatedActor;
import it.picus.identity.api.CurrentActorProvider;
import it.picus.organizations.api.OrganizationLookup;
import it.picus.organizations.domain.DemoOrganizationIds;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties =
        "PICUS_DEMO_PASSWORD=password-sintetica-solo-test")
@Import(ActorAndOrganizationTest.TestConfig.class)
class ActorAndOrganizationTest {

    private static final String PASSWORD =
            "password-sintetica-solo-test";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrganizationLookup organizationLookup;

    // Login reale: conserva il Principal nella sessione
    private MockHttpSession login(String username) throws Exception {

        MvcResult result = mvc.perform(formLogin()
                        .user(username)
                        .password(PASSWORD))
                .andExpect(authenticated().withUsername(username))
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);

        assertNotNull(session);
        return session;
    }

    // Il Principal di OPT A determina l'organizationId
    @Test
    void optAOrganizationComesFromSession() throws Exception {

        mvc.perform(get("/__test/actor")
                        .session(login("opt_a"))
                        .param("organizationId",
                                DemoOrganizationIds.OPT_B.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("opt_a"))
                .andExpect(jsonPath("$.role").value("OPT"))
                .andExpect(jsonPath("$.organizationId")
                        .value(DemoOrganizationIds.OPT_A.toString()));
    }

    // Il Principal di OPT B determina un organizationId diverso
    @Test
    void optBOrganizationComesFromSession() throws Exception {

        mvc.perform(get("/__test/actor")
                        .session(login("opt_b"))
                        .param("organizationId",
                                DemoOrganizationIds.OPT_A.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("opt_b"))
                .andExpect(jsonPath("$.role").value("OPT"))
                .andExpect(jsonPath("$.organizationId")
                        .value(DemoOrganizationIds.OPT_B.toString()));
    }

    // Il BANCO non appartiene a una OPT
    @Test
    void bancoHasNoOrganizationId() throws Exception {

        mvc.perform(get("/__test/actor")
                        .session(login("banco")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("banco"))
                .andExpect(jsonPath("$.role").value("BANCO"))
                .andExpect(jsonPath("$.organizationId").value(""));
    }

    // Lookup organizzazioni conosciute e sconosciute
    @Test
    void organizationLookupRecognizesDemoOrganizations() {

        assertNotEquals(
                DemoOrganizationIds.OPT_A,
                DemoOrganizationIds.OPT_B);

        assertTrue(organizationLookup.existsById(
                DemoOrganizationIds.OPT_A));

        assertTrue(organizationLookup.existsById(
                DemoOrganizationIds.OPT_B));

        UUID unknown = UUID.randomUUID();
        while (organizationLookup.existsById(unknown)) {
            unknown = UUID.randomUUID();
        }

        assertFalse(organizationLookup.existsById(unknown));
    }

    // Questo controller esiste solo durante i test
    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        ActorProbeController actorProbeController(
                CurrentActorProvider provider) {
            return new ActorProbeController(provider);
        }
    }

    @RestController
    static class ActorProbeController {

        private final CurrentActorProvider provider;

        ActorProbeController(CurrentActorProvider provider) {
            this.provider = provider;
        }

        @GetMapping(
                value = "/__test/actor",
                produces = MediaType.APPLICATION_JSON_VALUE)
        Map<String, String> actor() {

            AuthenticatedActor current = provider.currentActor();

            return Map.of(
                    "username", current.username(),
                    "role", current.role().name(),
                    "organizationId",
                    current.organizationId()
                            .map(UUID::toString)
                            .orElse("")
            );
        }
    }
}
