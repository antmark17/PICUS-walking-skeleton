package it.picus.bootstrap;
<<<<<<< HEAD

import it.picus.testsupport.RequestsExternalDependenciesTestConfiguration;
=======
 
>>>>>>> origin/feature/identity-organizations
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
 
@SpringBootTest
@AutoConfigureMockMvc
@Import(RequestsExternalDependenciesTestConfiguration.class)
class HealthControllerTests {
 
    @Autowired
    MockMvc mvc;
 
    @Test
    void healthIsAccessible() throws Exception {
        mvc.perform(get("/health")
                        .with(user("banco").roles("BANCO")))
                .andExpect(status().isOk())
                .andExpect(content().string("PICUS baseline avviata"));
    }
 
    @Test
    void unauthenticatedApplicationRoutesRedirectToLogin() throws Exception {
        mvc.perform(get("/opt/requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}