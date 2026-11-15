package it.picus.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Solo avvio comune: blocca le rotte applicative finche il Gruppo 1
 * non implementa autenticazione e autorizzazioni reali.
 * Non aggiunge utenti o credenziali dimostrative.
 */
@Configuration
public class BootstrapSecurityConfig {
    @Bean
    SecurityFilterChain bootstrapSecurity(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/error").permitAll()
                .anyRequest().denyAll())
            .build();
    }
}
