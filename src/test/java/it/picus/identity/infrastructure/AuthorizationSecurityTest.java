
package it.picus.identity.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties =
        "PICUS_DEMO_PASSWORD=password-sintetica-solo-test")
class AuthorizationSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsService users;

    // T09 - Le OPT non possono accedere al BANCO
    @ParameterizedTest
    @ValueSource(strings = {"opt_a", "opt_b"})
    void t09OptCannotAccessBanco(String username) throws Exception {

        mvc.perform(get("/banco/requests")
                        .with(user(users.loadUserByUsername(username))))
                .andExpect(status().isForbidden());

        mvc.perform(post("/banco/requests/123/start")
                        .with(user(users.loadUserByUsername(username)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // Il BANCO non può accedere agli endpoint OPT
    @Test
    void bancoCannotAccessOpt() throws Exception {

        mvc.perform(get("/opt/requests")
                        .with(user(users.loadUserByUsername("banco"))))
                .andExpect(status().isForbidden());

        mvc.perform(post("/opt/requests")
                        .with(user(users.loadUserByUsername("banco")))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // Richiesta OPT senza CSRF
    @Test
    void optPostWithoutCsrfIsForbidden() throws Exception {

        mvc.perform(post("/opt/requests")
                        .with(user(users.loadUserByUsername("opt_a"))))
                .andExpect(status().isForbidden());
    }

    // Richiesta BANCO senza CSRF
    @Test
    void bancoPostWithoutCsrfIsForbidden() throws Exception {

        mvc.perform(post("/banco/requests/123/start")
                        .with(user(users.loadUserByUsername("banco"))))
                .andExpect(status().isForbidden());
    }

    // Logout senza CSRF
    @Test
    void logoutWithoutCsrfIsForbidden() throws Exception {

        mvc.perform(post("/logout")
                        .with(user(users.loadUserByUsername("opt_a"))))
                .andExpect(status().isForbidden());
    }
}
