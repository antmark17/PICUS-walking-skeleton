
package it.picus.identity.infrastructure;

import it.picus.identity.api.ActorRole;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public final class RoleBasedAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final RedirectStrategy redirectStrategy =
            new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof PicusUserPrincipal principal)) {
            throw new IllegalStateException(
                    "Principal PICUS autenticato non disponibile"
            );
        }

        String destination = switch (principal.role()) {
            case OPT -> "/opt/requests";
            case BANCO -> "/banco/requests";
        };

        redirectStrategy.sendRedirect(
                request, response, destination
        );
    }
}
