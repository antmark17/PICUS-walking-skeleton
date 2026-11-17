
package it.picus.identity.infrastructure;

import it.picus.identity.api.ActorRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Principal Spring Security per il Walking Skeleton PICUS.
 *
 * Conserva il ruolo e l'organizzazione dell'utente.
 * L'organizationId non proviene mai dal browser.
 */
public record PicusUserPrincipal(
        String username,
        String password,
        ActorRole role,
        UUID organizationId
) implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    public PicusUserPrincipal {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(role, "role");

        if (username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Username e password codificata sono obbligatori"
            );
        }

        if (role == ActorRole.OPT && organizationId == null) {
            throw new IllegalArgumentException(
                    "Una OPT deve avere organizationId"
            );
        }

        if (role == ActorRole.BANCO && organizationId != null) {
            throw new IllegalArgumentException(
                    "Il Banco non deve avere organizationId"
            );
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "PicusUserPrincipal[username=" + username
                + ", role=" + role
                + ", organizationId=" + organizationId + "]";
    }
}
