package it.picus.requests.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Controlli della sola UI di login sul branch G1+G2+G3 integrato.
 * Non disattiva CSRF o Spring Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "PICUS_DEMO_PASSWORD=password-esclusivamente-test")
class PicusLoginPageMvcTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void loginPubblicoMostraGraficaEFormProtetto() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Accedi a PICUS")))
                .andExpect(content().string(containsString("/assets/banco-alimentare.svg")))
                .andExpect(content().string(containsString("/css/picus-login.css")))
                .andExpect(content().string(containsString("/js/picus-login.js")))
                .andExpect(content().string(containsString("name=\"username\"")))
                .andExpect(content().string(containsString("name=\"password\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("Credenziali di test"))));
    }

    @Test
    void errorMostraMessaggioSenzaEsporreDettagli() throws Exception {
        mvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nome utente o password non corretti")));
    }

    @Test
    void logoutMostraConferma() throws Exception {
        mvc.perform(get("/login").param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hai effettuato l’uscita correttamente")));
    }

    @Test
    void fileGraficiSonoAccessibiliSenzaLogin() throws Exception {
        mvc.perform(get("/css/picus-login.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".login-content")));
        mvc.perform(get("/js/picus-login.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("togglePassword")));
        mvc.perform(get("/assets/banco-alimentare.svg"))
                .andExpect(status().isOk());
    }
}
