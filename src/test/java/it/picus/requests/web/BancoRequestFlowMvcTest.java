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
import java.util.List;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Slice test MVC: usa G2 simulato e disabilita i filtri SOLO nel test.
 * Nessun cambiamento alla SecurityFilterChain reale, nessun dato fittizio in produzione.
 */
@WebMvcTest(controllers = BancoRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BancoRequestFlowMvcTest {
    private static final UUID ID1 = UUID.fromString("a92e62aa-2039-4b08-ae4c-6cf0436bbfc2");
    private static final UUID ID2 = UUID.fromString("7256426d-5dc9-4b55-9acb-9052a1fe04a9");
    private static final UUID OPT1 = UUID.fromString("bbb977f2-3a3b-4d0a-a5b6-8da7b7e0e63a");
    private static final UUID OPT2 = UUID.fromString("666f8dd3-cf0d-4e85-a943-ac9367838702");

    @Autowired MockMvc mvc;
    @MockitoBean RequestUseCases useCases;

    private static RequestView request(UUID id, UUID organizationId, String measure, RequestStatus status) {
        return new RequestView(id, organizationId, measure, status, Instant.parse("2026-11-16T10:30:00Z"));
    }

    @Test
    void listaRenderizzaDatiRealiDelContrattoEAnteprima() throws Exception {
        when(useCases.listForBanco()).thenReturn(List.of(
                request(ID1, OPT1, "Misura A", RequestStatus.DA_VALUTARE),
                request(ID2, OPT2, "Misura B", RequestStatus.IN_ELABORAZIONE)));
        MvcResult result = mvc.perform(get("/banco/requests").principal(() -> "banco"))
                .andExpect(status().isOk())
                .andExpect(view().name("banco/requests"))
                .andExpect(content().string(containsString("Richieste ricevute")))
                .andExpect(content().string(containsString("OPT · bbb977f2")))
                .andExpect(content().string(containsString("Misura A")))
                .andExpect(content().string(containsString("In elaborazione")))
                .andExpect(content().string(containsString("/banco/requests/" + ID1 + "/start")))
                .andExpect(content().string(not(containsString("th:each="))))
                .andReturn();
        verify(useCases).listForBanco();
        savePreview("banco-requests.html", result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void listaVuotaMostraStatoVuoto() throws Exception {
        when(useCases.listForBanco()).thenReturn(List.of());
        mvc.perform(get("/banco/requests").principal(() -> "banco"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nessuna richiesta")));
    }

    @Test
    void dettaglioMostraIdentificativoOPTESalvaAnteprima() throws Exception {
        when(useCases.getForBanco(ID1)).thenReturn(request(ID1, OPT1, "Misura A", RequestStatus.DA_VALUTARE));
        MvcResult result = mvc.perform(get("/banco/requests/{id}", ID1).principal(() -> "banco"))
                .andExpect(status().isOk())
                .andExpect(view().name("banco/request-detail"))
                .andExpect(content().string(containsString(ID1.toString())))
                .andExpect(content().string(containsString(OPT1.toString())))
                .andExpect(content().string(containsString("Avvia elaborazione")))
                .andReturn();
        verify(useCases).getForBanco(ID1);
        savePreview("banco-request-detail.html", result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void dettaglioInElaborazioneNonOffreAvvioAbilitato() throws Exception {
        when(useCases.getForBanco(ID2)).thenReturn(request(ID2, OPT2, "Misura B", RequestStatus.IN_ELABORAZIONE));
        mvc.perform(get("/banco/requests/{id}", ID2).principal(() -> "banco"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("In elaborazione")))
                .andExpect(content().string(not(containsString("/banco/requests/" + ID2 + "/start"))));
    }

    @Test
    void postAvvioChiamaIlServizioEReindirizzaConConferma() throws Exception {
        when(useCases.getForBanco(ID1)).thenReturn(request(ID1, OPT1, "Misura A", RequestStatus.DA_VALUTARE));
        mvc.perform(post("/banco/requests/{id}/start", ID1).principal(() -> "banco"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/banco/requests/" + ID1))
                .andExpect(flash().attribute("startSuccess", true));
        verify(useCases).getForBanco(ID1);
        verify(useCases).startProcessing(ID1);
    }

    @Test
    void postRipetutoNonAvviaDiNuovo() throws Exception {
        when(useCases.getForBanco(ID2)).thenReturn(request(ID2, OPT2, "Misura B", RequestStatus.IN_ELABORAZIONE));
        mvc.perform(post("/banco/requests/{id}/start", ID2).principal(() -> "banco"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/banco/requests/" + ID2))
                .andExpect(flash().attribute("alreadyProcessing", true));
        verify(useCases, never()).startProcessing(any(UUID.class));
    }

    @Test
    void parametroOrganizzazioneNelPostNonInfluenzaIlServizio() throws Exception {
        when(useCases.getForBanco(ID1)).thenReturn(request(ID1, OPT1, "Misura A", RequestStatus.DA_VALUTARE));
        mvc.perform(post("/banco/requests/{id}/start", ID1)
                        .param("organizationId", OPT2.toString()).principal(() -> "banco"))
                .andExpect(status().is3xxRedirection());
        verify(useCases).startProcessing(ID1);
    }

    @Test
    void thymeleafEscapaIlTestoDellaMisura() throws Exception {
        String unsafe = "<script>alert(1)</script>";
        when(useCases.getForBanco(ID1)).thenReturn(request(ID1, OPT1, unsafe, RequestStatus.DA_VALUTARE));
        mvc.perform(get("/banco/requests/{id}", ID1).principal(() -> "banco"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString(unsafe))));
    }

    /** I file di anteprima sono locali in target/, cartella ignorata da Git. */
    private static void savePreview(String filename, String html) throws IOException {
        Path target = Path.of("target", "picus-preview");
        Files.createDirectories(target);
        String local = html.replace("href=\"/css/", "href=\"css/")
                .replace("src=\"/js/", "src=\"js/")
                .replace("src=\"/assets/", "src=\"assets/");
        Path page = target.resolve(filename);
        Files.writeString(page, local, StandardCharsets.UTF_8);
        for (String asset : new String[]{
                "css/picus.css", "js/picus-layout.js", "js/picus-banco.js", "assets/banco-alimentare.svg"}) {
            Path dest = target.resolve(asset);
            Files.createDirectories(dest.getParent());
            try (InputStream input = new ClassPathResource("static/" + asset).getInputStream()) {
                Files.copy(input, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        System.out.println("Anteprima PICUS: " + page.toAbsolutePath());
    }
}
