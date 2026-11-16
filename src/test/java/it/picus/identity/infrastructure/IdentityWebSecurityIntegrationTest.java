package it.picus.identity.infrastructure;
 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
 
import java.util.Arrays;
 
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties =
        "PICUS_DEMO_PASSWORD=password-sintetica-solo-test")
class IdentityWebSecurityIntegrationTest {
 
    private static final String PASSWORD =
            "password-sintetica-solo-test";
 
    @Autowired
    private MockMvc mvc;
 
    @Test
    void loginPageIsPublicAndContainsCsrf() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(
                        containsString("name=\"_csrf\"")
                ));
    }
 
    @Test
    void correctUsersRedirectToCorrectAreas() throws Exception {
        mvc.perform(formLogin()
                        .user("opt_a").password(PASSWORD))
                .andExpect(redirectedUrl("/opt/requests"));
 
        mvc.perform(formLogin()
                        .user("opt_b").password(PASSWORD))
                .andExpect(redirectedUrl("/opt/requests"));
 
        mvc.perform(formLogin()
                        .user("banco").password(PASSWORD))
                .andExpect(redirectedUrl("/banco/requests"));
    }
 
    @Test
    void wrongPasswordRedirectsToError() throws Exception {
        mvc.perform(formLogin()
                        .user("opt_a").password("wrong"))
                .andExpect(redirectedUrl("/login?error"));
    }
 
    @Test
    void loginWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/login")
                        .param("username", "opt_a")
                        .param("password", PASSWORD))
                .andExpect(status().isForbidden());
    }
 
    @Test
    void crossRoleAccessIsForbidden() throws Exception {
        mvc.perform(get("/banco/requests")
                        .with(user("opt_a").roles("OPT")))
                .andExpect(status().isForbidden());
 
        mvc.perform(get("/opt/requests")
                        .with(user("banco").roles("BANCO")))
                .andExpect(status().isForbidden());
    }
 
    @Test
    void logoutInvalidatesSessionAndDeletesCookie()
            throws Exception {
 
        MvcResult login = mvc.perform(
                formLogin().user("opt_a").password(PASSWORD)
        ).andReturn();
 
        MockHttpSession session =
                (MockHttpSession)
                        login.getRequest().getSession(false);
 
        assertNotNull(session);
 
        MvcResult result = mvc.perform(
                post("/logout").session(session).with(csrf())
        ).andExpect(redirectedUrl("/login?logout")).andReturn();
 
        assertTrue(session.isInvalid());
 
        assertTrue(Arrays.stream(
                result.getResponse().getCookies()
        ).anyMatch(cookie ->
                "JSESSIONID".equals(cookie.getName())
                        && cookie.getMaxAge() == 0
        ));
    }
 
    @Test
    void optCannotAccessBancoRequestDetails() throws Exception {
        mvc.perform(get("/banco/requests/123")
                        .with(user("opt_a").roles("OPT")))
                .andExpect(status().isForbidden());
        }
 
    @Test
    void optCannotStartBancoRequestEvenWithCsrf() throws Exception {
        mvc.perform(post("/banco/requests/123/start")
                        .with(user("opt_a").roles("OPT"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        }
 
    @Test
    void bancoCannotAccessOptRequestEndpoints() throws Exception {
        mvc.perform(get("/opt/requests/123")
                        .with(user("banco").roles("BANCO")))
                .andExpect(status().isForbidden());
 
        mvc.perform(post("/opt/requests")
                        .with(user("banco").roles("BANCO"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        }

@Test
void logoutWithoutCsrfIsForbidden() throws Exception {
    mvc.perform(post("/logout")
                    .with(user("opt_a").roles("OPT")))
            .andExpect(status().isForbidden());
}

@Test
void optCannotCreateRequestWithoutCsrf() throws Exception {
    mvc.perform(post("/opt/requests")
                    .with(user("opt_a").roles("OPT")))
            .andExpect(status().isForbidden());
}

@Test
void bancoCannotStartRequestWithoutCsrf() throws Exception {
    mvc.perform(post("/banco/requests/123/start")
                    .with(user("banco").roles("BANCO")))
            .andExpect(status().isForbidden());
}

}