package it.picus.requests.web;

import it.picus.requests.api.RequestUseCases;
import it.picus.requests.api.RequestView;
import it.picus.requests.domain.RequestStatus;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Solo i filtri di questo slice test sono disabilitati; in produzione Spring Security resta attivo.
 * G2 viene simulato soltanto in test tramite @MockitoBean.
 */
@WebMvcTest(controllers = {OptNewRequestController.class, OptRequestDetailController.class})
@AutoConfigureMockMvc(addFilters = false)
class OptNewRequestFlowMvcTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RequestUseCases requestUseCases;

    @Test
    void getNuovaRichiestaRenderizzaIlFormESalvaAnteprima() throws Exception {
        MvcResult result = mvc.perform(get("/opt/requests/new").principal(() -> "opt-a"))
                .andExpect(status().isOk())
                .andExpect(view().name("opt/new-request"))
                .andExpect(content().string(containsString("Nuova richiesta")))
                .andExpect(content().string(containsString("Misura A")))
                .andExpect(content().string(containsString("Misura B")))
                .andExpect(content().string(containsString("name=\"misuraRichiesta\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(not(containsString("th:field="))))
                .andReturn();
        savePreview("opt-new-request.html", result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void postSenzaMisuraRestaSulFormEIndicaLerrore() throws Exception {
        mvc.perform(post("/opt/requests").principal(() -> "opt-a")
                        .param("misuraRichiesta", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("opt/new-request"))
                .andExpect(model().attributeHasFieldErrors("requestForm", "misuraRichiesta"))
                .andExpect(content().string(containsString("Seleziona una misura")));
        verify(requestUseCases, never()).submitRequest(anyString());
    }

    @Test
    void postMisuraFuoriElencoRifiutatoAncheLatoServer() throws Exception {
        mvc.perform(post("/opt/requests").principal(() -> "opt-a")
                        .param("misuraRichiesta", "Misura non prevista"))
                .andExpect(status().isOk())
                .andExpect(view().name("opt/new-request"))
                .andExpect(model().attributeHasFieldErrors("requestForm", "misuraRichiesta"))
                .andExpect(content().string(containsString("Seleziona una misura valida")));
        verify(requestUseCases, never()).submitRequest(anyString());
    }

    @Test
    void postValidoChiamaIlServizioEReindirizzaAlDettaglio() throws Exception {
        UUID id = UUID.fromString("a92e62aa-2039-4b08-ae4c-6cf0436bbfc2");
        when(requestUseCases.submitRequest("Misura B")).thenReturn(id);

        mvc.perform(post("/opt/requests").principal(() -> "opt-a")
                        .param("misuraRichiesta", "Misura B"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/opt/requests/" + id));
        verify(requestUseCases).submitRequest("Misura B");
    }

    @Test
    void dettaglioLeggeSoloGetMineEGeneraLaSecondaAnteprima() throws Exception {
        UUID id = UUID.fromString("a92e62aa-2039-4b08-ae4c-6cf0436bbfc2");
        when(requestUseCases.getMine(id)).thenReturn(new RequestView(
                id, UUID.randomUUID(), "Misura B", RequestStatus.DA_VALUTARE,
                Instant.parse("2026-11-16T10:30:00Z")));

        MvcResult result = mvc.perform(get("/opt/requests/{id}", id).principal(() -> "opt-a"))
                .andExpect(status().isOk())
                .andExpect(view().name("opt/request-detail"))
                .andExpect(content().string(containsString("Misura B")))
                .andExpect(content().string(containsString("Da valutare")))
                .andExpect(content().string(containsString(id.toString())))
                .andReturn();
        verify(requestUseCases).getMine(id);
        savePreview("opt-request-detail.html", result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void dettaglioEscapaTestoNonSicuro() throws Exception {
        UUID id = UUID.randomUUID();
        String unsafe = "<script>alert(1)</script>";
        when(requestUseCases.getMine(id)).thenReturn(new RequestView(
                id, UUID.randomUUID(), unsafe, RequestStatus.IN_ELABORAZIONE,
                Instant.parse("2026-11-16T10:30:00Z")));
        mvc.perform(get("/opt/requests/{id}", id).principal(() -> "opt-a"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString(unsafe))));
    }

    /** Anteprime solo locali: target/ e' ignorata da Git. */
    private static void savePreview(String filename, String html) throws IOException {
        Path target = Path.of("target", "picus-preview");
        Files.createDirectories(target);
        String local = html.replace("href=\"/css/", "href=\"css/")
                .replace("src=\"/js/", "src=\"js/")
                .replace("src=\"/assets/", "src=\"assets/");
        Path page = target.resolve(filename);
        Files.writeString(page, local, StandardCharsets.UTF_8);
        for (String asset : new String[] {"css/picus.css", "js/picus-layout.js", "assets/banco-alimentare.svg"}) {
            Path dest = target.resolve(asset);
            Files.createDirectories(dest.getParent());
            try (InputStream input = new ClassPathResource("static/" + asset).getInputStream()) {
                Files.copy(input, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        System.out.println("Anteprima PICUS: " + page.toAbsolutePath());
    }
}
