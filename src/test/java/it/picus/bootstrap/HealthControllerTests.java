package it.picus.bootstrap;

import it.picus.testsupport.RequestsExternalDependenciesTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Import(RequestsExternalDependenciesTestConfiguration.class)
class HealthControllerTests {
    @Autowired MockMvc mvc;
    @Test
    void healthIsAccessible() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("PICUS baseline avviata"));
    }
    @Test
    void applicationRoutesAreDeniedBeforeSecurityImplementation() throws Exception {
        mvc.perform(get("/opt/requests"))
                .andExpect(status().isForbidden());
    }
}
