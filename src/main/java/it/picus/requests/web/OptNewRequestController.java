package it.picus.requests.web;

import it.picus.requests.api.RequestUseCases;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Interfaccia OPT per l'invio di una richiesta.
 * L'organizzazione NON arriva dal form: e' ricavata dal principal nel servizio G2.
 * Le tre misure sono un insieme temporaneo da concordare con il team, non il catalogo ufficiale.
 */
@Controller
public class OptNewRequestController {

    private static final List<String> MEASURES = List.of("Misura A", "Misura B", "Misura C");
    private final ObjectProvider<RequestUseCases> requestUseCases;

    public OptNewRequestController(ObjectProvider<RequestUseCases> requestUseCases) {
        this.requestUseCases = requestUseCases;
    }

    @GetMapping("/opt/requests/new")
    public String newRequest(Model model, Principal principal) {
        model.addAttribute("requestForm", new NewRequestForm());
        prepareModel(model, principal);
        return "opt/new-request";
    }

    @PostMapping("/opt/requests")
    public String submitRequest(@Valid @ModelAttribute("requestForm") NewRequestForm requestForm,
                                BindingResult errors,
                                Model model,
                                Principal principal) {
        if (errors.hasErrors()) {
            prepareModel(model, principal);
            return "opt/new-request";
        }

        // Nessun organizationId, stato o identificativo assistito nel payload.
        UUID id = requireUseCases().submitRequest(requestForm.getMisuraRichiesta());
        return "redirect:/opt/requests/" + id;
    }

    private void prepareModel(Model model, Principal principal) {
        model.addAttribute("measures", MEASURES);
        model.addAttribute("accountName", principal != null ? principal.getName() : "Organizzazione partner");
    }

    private RequestUseCases requireUseCases() {
        RequestUseCases service = requestUseCases.getIfAvailable();
        if (service == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servizio richieste non disponibile");
        }
        return service;
    }

    public static class NewRequestForm {
        @NotBlank(message = "Seleziona una misura")
        @Pattern(regexp = "Misura A|Misura B|Misura C", message = "Seleziona una misura valida")
        private String misuraRichiesta;

        public String getMisuraRichiesta() {
            return misuraRichiesta;
        }

        public void setMisuraRichiesta(String misuraRichiesta) {
            this.misuraRichiesta = misuraRichiesta;
        }
    }
}
