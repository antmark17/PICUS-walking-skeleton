package it.picus.requests.web;

import it.picus.requests.api.RequestUseCases;
import it.picus.requests.api.RequestView;
import it.picus.requests.domain.RequestStatus;
import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Primo controller web G3: elenco richieste della OPT autenticata.
 * Non accede a JPA e non determina l'organizationId dal browser.
 * L'ObjectProvider rende avviabile la baseline finché il bean G2 non è integrato;
 * non sostituisce il servizio reale con richieste fittizie.
 */
@Controller
public class OptRequestController {
    private static final DateTimeFormatter IT_DATE = DateTimeFormatter
            .ofPattern("dd/MM/yyyy, HH:mm", Locale.ITALIAN)
            .withZone(ZoneId.of("Europe/Rome"));

    private final ObjectProvider<RequestUseCases> requestUseCases;

    public OptRequestController(ObjectProvider<RequestUseCases> requestUseCases) {
        this.requestUseCases = requestUseCases;
    }

    @GetMapping("/opt/requests")
    public String listMine(Model model, Principal principal) {
        RequestUseCases service = requestUseCases.getIfAvailable();
        if (service == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servizio richieste non disponibile");
        }
        List<RequestRow> rows = service.listMine().stream()
                .sorted(Comparator.comparing(RequestView::createdAt).reversed())
                .map(OptRequestController::toRow)
                .toList();
        model.addAttribute("requests", rows);
        model.addAttribute("accountName", principal != null ? principal.getName() : "Organizzazione partner");
        return "opt/requests";
    }

    private static RequestRow toRow(RequestView v) {
        UUID id = v.id();
        String fullId = id.toString();
        String shortId = fullId.substring(0, 8) + "…" + fullId.substring(fullId.length() - 4);
        String stato = v.status().name();
        String statoLabel = v.status() == RequestStatus.DA_VALUTARE ? "Da valutare" : "In elaborazione";
        String searchText = fullId + " " + v.misuraRichiesta();
        return new RequestRow(id, shortId, v.misuraRichiesta(), IT_DATE.format(v.createdAt()),
                stato, statoLabel, searchText);
    }

    /** Dati di sola presentazione; il dominio resta di G2. */
    public record RequestRow(UUID id, String idBreve, String misuraRichiesta, String dataInvio,
                             String stato, String statoLabel, String searchText) {}
}
