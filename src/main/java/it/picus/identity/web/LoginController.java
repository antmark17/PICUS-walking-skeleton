
package it.picus.identity.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
