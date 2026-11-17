package it.picus.testsupport;

import java.util.Optional;

import it.picus.identity.api.ActorRole;
import it.picus.identity.api.AuthenticatedActor;
import it.picus.identity.api.CurrentActorProvider;
import it.picus.organizations.api.OrganizationLookup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class RequestsExternalDependenciesTestConfiguration {
    @Bean
    @Primary
    CurrentActorProvider testCurrentActorProvider() {
        return () -> new AuthenticatedActor(
                "test-banco",
                ActorRole.BANCO,
                Optional.empty());
    }

    @Bean
    @Primary
    OrganizationLookup testOrganizationLookup() {
        return organizationId -> true;
    }
}
