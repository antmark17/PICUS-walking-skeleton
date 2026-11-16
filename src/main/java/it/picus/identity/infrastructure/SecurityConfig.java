
package it.picus.identity.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configurazione iniziale della sicurezza PICUS.
 *
 * Giorno 1:
 * - PasswordEncoder con BCrypt
 * - Abilitazione della sicurezza sui metodi
 *
 * La SecurityFilterChain provvisoria rimane
 * in BootstrapSecurityConfig.
 *
 * Login, logout e autorizzazioni HTTP saranno
 * completati nelle giornate successive.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
