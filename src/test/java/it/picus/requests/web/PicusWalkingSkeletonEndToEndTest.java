package it.picus.requests.web;

import it.picus.organizations.domain.DemoOrganizationIds;
import it.picus.requests.domain.RequestStatus;
import it.picus.requests.domain.ServiceRequestRepository;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Verifica integrata delle tre aree G1/G2/G3, con Spring Security, MVC e H2.
 * Non vengono usati mock dei servizi: ogni richiesta attraversa il sistema reale.
 * Le proprieta' in src/test/resources/application.properties usano H2 in memoria.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "PICUS_DEMO_PASSWORD=password-sintetica-per-e2e")
class PicusWalkingSkeletonEndToEndTest {

    private static final String PASSWORD = "password-sintetica-per-e2e";
    private static final String OPT_DETAIL_PREFIX = "/opt/requests/";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ServiceRequestRepository repository;

    @Test
    void percorsoCompletoOptABancoOptAEIsolamentoOptB() throws Exception {
        // G1: sessioni/autenticazioni indipendenti per i tre utenti.
        MockHttpSession optA = login("opt_a", "/opt/requests");

        // G3 -> G2: invio reale (con CSRF); proprietario derivato dal principal.
        mvc.perform(get("/opt/requests/new").session(optA))
                .andExpect(status().isOk());
        MvcResult create = mvc.perform(post("/opt/requests")
                        .session(optA)
                        .with(csrf())
                        .param("misuraRichiesta", "Misura A"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirect = Objects.requireNonNull(create.getResponse().getRedirectedUrl());
        assertTrue(redirect.startsWith(OPT_DETAIL_PREFIX), redirect);
        UUID id = UUID.fromString(redirect.substring(OPT_DETAIL_PREFIX.length()));
        assertEquals(DemoOrganizationIds.OPT_A,
                repository.findById(id).orElseThrow().getOrganizationId());
        assertEquals(RequestStatus.DA_VALUTARE,
                repository.findById(id).orElseThrow().getStatus());

        mvc.perform(get(OPT_DETAIL_PREFIX + id).session(optA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Misura A")))
                .andExpect(content().string(containsString("Da valutare")));

        // Banco: vede la richiesta e ne avvia davvero l'elaborazione.
        MockHttpSession banco = login("banco", "/banco/requests");
        mvc.perform(get("/banco/requests").session(banco))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
        mvc.perform(post("/banco/requests/{id}/start", id)
                        .session(banco)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/banco/requests/" + id))
                .andExpect(flash().attribute("startSuccess", true));
        assertEquals(RequestStatus.IN_ELABORAZIONE,
                repository.findById(id).orElseThrow().getStatus());

        // G3: entrambe le viste leggono il nuovo stato dal database.
        mvc.perform(get("/banco/requests/{id}", id).session(banco))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("In elaborazione")));
        mvc.perform(get(OPT_DETAIL_PREFIX + id).session(optA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("In elaborazione")));

        // QS-SEC-01: OPT B non deve vedere ne' aprire la richiesta di OPT A.
        MockHttpSession optB = login("opt_b", "/opt/requests");
        mvc.perform(get("/opt/requests").session(optB))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(id.toString()))));
        mvc.perform(get(OPT_DETAIL_PREFIX + id).session(optB))
                .andExpect(status().isNotFound());

        // La pagina protetta non sostituisce i controlli lato server.
        mvc.perform(get("/banco/requests").session(optB))
                .andExpect(status().isForbidden());
    }

    @Test
    void iFormMantengonoCsrfEControlliDiValidazione() throws Exception {
        MockHttpSession optA = login("opt_a", "/opt/requests");

        // Un POST senza token CSRF e' vietato anche con login valido.
        mvc.perform(post("/opt/requests")
                        .session(optA)
                        .param("misuraRichiesta", "Misura B"))
                .andExpect(status().isForbidden());

        // Il server rifiuta valori fuori dall'insieme consentito.
        mvc.perform(post("/opt/requests")
                        .session(optA)
                        .with(csrf())
                        .param("misuraRichiesta", "valore-non-previsto"))
                .andExpect(status().isOk())
                .andExpect(view().name("opt/new-request"))
                .andExpect(content().string(containsString("Seleziona una misura valida")));
    }

    @Test
    void ilLoginPubblicoCaricaAncheIlFoglioDiStile() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/css/picus-login.css"))
                .andExpect(status().isOk());
    }

    private MockHttpSession login(String username, String redirectAtteso) throws Exception {
        MvcResult result = mvc.perform(formLogin()
                        .user(username)
                        .password(PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectAtteso))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session, "Login senza sessione per " + username);
        return session;
    }
}
