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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Verifica il rendering effettivo del template Thymeleaf, isolato dai gruppi G1 e G2.
 * I filtri di sicurezza sono esclusi SOLO da questo test; la sicurezza dell'app
 * avviata normalmente rimane invariata.
 */
@WebMvcTest(controllers = OptRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class OptRequestsThymeleafRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestUseCases requestUseCases;

    @Test
    void renderizzaPaginaConRichiestaESalvaAnteprimaApribileNelBrowser() throws Exception {
        UUID id = UUID.fromString("bca5a767-8802-4121-9102-670104bde24b");
        RequestView richiesta = new RequestView(
                id,
                UUID.fromString("b2ade330-609c-4c49-b82a-1534cb445703"),
                "Sostegno alimentare",
                RequestStatus.DA_VALUTARE,
                Instant.parse("2026-11-16T10:30:00Z")
        );
        when(requestUseCases.listMine()).thenReturn(List.of(richiesta));

        MvcResult risultato = mockMvc.perform(get("/opt/requests").principal(() -> "opt-a"))
                .andExpect(status().isOk())
                .andExpect(view().name("opt/requests"))
                .andExpect(content().string(containsString("Le mie richieste")))
                .andExpect(content().string(containsString("Sostegno alimentare")))
                .andExpect(content().string(containsString("Da valutare")))
                .andExpect(content().string(containsString(id.toString())))
                .andExpect(content().string(not(containsString("th:each="))))
                .andReturn();

        salvaAnteprima(risultato.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void renderizzaCorrettamenteAncheLaListaVuota() throws Exception {
        when(requestUseCases.listMine()).thenReturn(List.of());

        mockMvc.perform(get("/opt/requests").principal(() -> "opt-a"))
                .andExpect(status().isOk())
                .andExpect(view().name("opt/requests"))
                .andExpect(content().string(containsString("0 richieste")))
                .andExpect(content().string(not(containsString("class=\"request-row\""))));
    }

    @Test
    void testoDellaMisuraVieneEscapatoDaThymeleaf() throws Exception {
        String testoNonSicuro = "<script>alert(1)</script>";
        when(requestUseCases.listMine()).thenReturn(List.of(new RequestView(
                UUID.randomUUID(), UUID.randomUUID(), testoNonSicuro,
                RequestStatus.IN_ELABORAZIONE, Instant.parse("2026-11-16T10:30:00Z")
        )));

        mockMvc.perform(get("/opt/requests").principal(() -> "opt-a"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("In elaborazione")))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString(testoNonSicuro))));
    }

    /**
     * L'anteprima e' generata in target/ (escluso da Git) e NON e' una pagina
     * dell'applicazione: serve soltanto a controllare il design in locale.
     */
    private static void salvaAnteprima(String html) throws IOException {
        Path cartella = Path.of("target", "picus-preview");
        Files.createDirectories(cartella);

        String htmlLocale = html
                .replace("href=\"/css/", "href=\"css/")
                .replace("src=\"/js/", "src=\"js/")
                .replace("src=\"/assets/", "src=\"assets/");
        Path fileHtml = cartella.resolve("opt-requests.html");
        Files.writeString(fileHtml, htmlLocale, StandardCharsets.UTF_8);

        copiaRisorsaStatica(cartella, "css/picus.css");
        copiaRisorsaStatica(cartella, "js/picus-layout.js");
        copiaRisorsaStatica(cartella, "js/picus-opt-requests.js");
        copiaRisorsaStatica(cartella, "assets/banco-alimentare.svg");

        System.out.println("Anteprima PICUS: " + fileHtml.toAbsolutePath());
    }

    private static void copiaRisorsaStatica(Path cartella, String percorso) throws IOException {
        Path destinazione = cartella.resolve(percorso);
        Files.createDirectories(destinazione.getParent());
        try (InputStream risorsa = new ClassPathResource("static/" + percorso).getInputStream()) {
            Files.copy(risorsa, destinazione, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
