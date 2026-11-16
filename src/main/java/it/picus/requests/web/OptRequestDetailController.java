package it.picus.requests.web;

import it.picus.requests.api.RequestUseCases;
import it.picus.requests.api.RequestView;
import it.picus.requests.domain.RequestStatus;
import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/** Dettaglio OPT, letto solo tramite il contratto G2 che verifica l'appartenenza. */
@Controller
public class OptRequestDetailController {
    private static final DateTimeFormatter IT_DATE = DateTimeFormatter
            .ofPattern("dd/MM/yyyy, HH:mm", Locale.ITALIAN)
            .withZone(ZoneId.of("Europe/Rome"));

    private final ObjectProvider<RequestUseCases> requestUseCases;

    public OptRequestDetailController(ObjectProvider<RequestUseCases> requestUseCases) {
        this.requestUseCases = requestUseCases;
    }

    @GetMapping("/opt/requests/{id}")
    public String detail(@PathVariable UUID id, Model model, Principal principal) {
        RequestUseCases service = requestUseCases.getIfAvailable();
        if (service == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servizio richieste non disponibile");
        }
        // getMine() del G2 limita l'accesso alla sola organizzazione autenticata.
        RequestView view = service.getMine(id);
        model.addAttribute("request", new RequestDetails(
                view.id(), view.misuraRichiesta(), IT_DATE.format(view.createdAt()),
                view.status() == RequestStatus.DA_VALUTARE ? "Da valutare" : "In elaborazione",
                view.status() == RequestStatus.DA_VALUTARE ? "to-evaluate" : "processing"));
        model.addAttribute("accountName", principal != null ? principal.getName() : "Organizzazione partner");
        return "opt/request-detail";
    }

    public record RequestDetails(UUID id, String misuraRichiesta, String dataInvio,
                                 String statoLabel, String statoClasse) {}
}
