package it.picus;

import it.picus.testsupport.RequestsExternalDependenciesTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(RequestsExternalDependenciesTestConfiguration.class)
class PicusApplicationTests {
    @Test
    void contextLoads() {}
}
