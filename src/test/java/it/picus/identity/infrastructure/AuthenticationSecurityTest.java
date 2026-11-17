
package it.picus.identity.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties =
        "PICUS_DEMO_PASSWORD=password-sintetica-solo-test")
class AuthenticationSecurityTest {

    private static final String PASSWORD =
            "password-sintetica-solo-test";

    @Autowired
    private MockMvc mvc;

    // T01 - Login BANCO
    @Test
    void t01BancoLogin() throws Exception {
        mvc.perform(formLogin()
                        .user("banco")
                        .password(PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("banco"))
                .andExpect(redirectedUrl("/banco/requests"));
    }

    // T02 - Login OPT A
    @Test
    void t02OptALogin() throws Exception {
        mvc.perform(formLogin()
                        .user("opt_a")
                        .password(PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("opt_a"))
                .andExpect(redirectedUrl("/opt/requests"));
    }

    // T03 - Password errata
    @Test
    void t03WrongPassword() throws Exception {
        MvcResult result = mvc.perform(formLogin()
                        .user("opt_a")
                        .password("password-errata"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);

        if (session != null) {
            assertNull(session.getAttribute(
                    "SPRING_SECURITY_CONTEXT"),
                    "La sessione non deve contenere un'autenticazione");
        }

        mvc.perform(session == null
                        ? get("/opt/requests")
                        : get("/opt/requests").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // Logout per BANCO e OPT
    @ParameterizedTest
    @CsvSource({
            "banco,/banco/requests",
            "opt_a,/opt/requests",
            "opt_b,/opt/requests"
    })
    void logoutInvalidatesSession(
            String username,
            String protectedPath
    ) throws Exception {

        MvcResult login = mvc.perform(formLogin()
                        .user(username)
                        .password(PASSWORD))
                .andExpect(authenticated().withUsername(username))
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) login.getRequest().getSession(false);

        assertNotNull(session);

        mvc.perform(post("/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        assertTrue(session.isInvalid());

        mvc.perform(get(protectedPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
