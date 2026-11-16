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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Area Banco: nessun accesso diretto a JPA e nessuna autorizzazione affidata alla UI. */
@Controller
@RequestMapping("/banco/requests")
public class BancoRequestController {
    private static final DateTimeFormatter IT_DATE = DateTimeFormatter
            .ofPattern("dd/MM/yyyy, HH:mm", Locale.ITALIAN)
            .withZone(ZoneId.of("Europe/Rome"));

    private final ObjectProvider<RequestUseCases> requestUseCases;

    public BancoRequestController(ObjectProvider<RequestUseCases> requestUseCases) {
        this.requestUseCases = requestUseCases;
    }

    @GetMapping
    public String list(Model model, Principal principal) {
        List<BancoRequestRow> rows = service().listForBanco().stream()
                .sorted(Comparator.comparing(RequestView::createdAt).reversed())
                .map(BancoRequestController::toRow)
                .toList();
        model.addAttribute("requests", rows);
        model.addAttribute("accountName", accountName(principal));
        return "banco/requests";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model, Principal principal) {
        RequestView request = service().getForBanco(id);
        model.addAttribute("request", new BancoRequestDetails(
                request.id(), request.organizationId(), organizationLabel(request.organizationId()),
                request.misuraRichiesta(), IT_DATE.format(request.createdAt()),
                request.status().name(), statusLabel(request.status()),
                request.status() == RequestStatus.DA_VALUTARE));
        model.addAttribute("accountName", accountName(principal));
        return "banco/request-detail";
    }

    @PostMapping("/{id}/start")
    public String start(@PathVariable UUID id, RedirectAttributes redirect) {
        RequestUseCases useCases = service();
        // Controllo anche lato server: un bottone disabilitato nel browser non basta.
        RequestView request = useCases.getForBanco(id);
        if (request.status() != RequestStatus.DA_VALUTARE) {
            redirect.addFlashAttribute("alreadyProcessing", true);
            return "redirect:/banco/requests/" + id;
        }
        // La transizione vera, atomica e autorizzata e' responsabilita' del G2.
        useCases.startProcessing(id);
        redirect.addFlashAttribute("startSuccess", true);
        return "redirect:/banco/requests/" + id;
    }

    private RequestUseCases service() {
        RequestUseCases bean = requestUseCases.getIfAvailable();
        if (bean == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servizio richieste non disponibile");
        }
        return bean;
    }

    private static String accountName(Principal principal) {
        return principal != null ? principal.getName() : "Banco Alimentare";
    }

    private static String organizationLabel(UUID organizationId) {
        // G2 espone un UUID, non il nome dell'OPT. Non inventiamo informazioni.
        return "OPT · " + organizationId.toString().substring(0, 8);
    }

    private static String statusLabel(RequestStatus status) {
        return status == RequestStatus.DA_VALUTARE ? "Da valutare" : "In elaborazione";
    }

    private static BancoRequestRow toRow(RequestView r) {
        String id = r.id().toString();
        return new BancoRequestRow(r.id(), id.substring(0, 8) + "…" + id.substring(id.length() - 4),
                r.organizationId(), organizationLabel(r.organizationId()),
                r.misuraRichiesta(), IT_DATE.format(r.createdAt()), r.status().name(),
                statusLabel(r.status()), r.status() == RequestStatus.DA_VALUTARE,
                id + " " + r.organizationId() + " " + r.misuraRichiesta());
    }

    /** Record di sola presentazione, senza modificare il dominio del Gruppo 2. */
    public record BancoRequestRow(UUID id, String idBreve, UUID organizationId,
                                  String organizationLabel, String misuraRichiesta,
                                  String dataInvio, String stato, String statoLabel,
                                  boolean avviabile, String searchText) {}

    public record BancoRequestDetails(UUID id, UUID organizationId, String organizationLabel,
                                      String misuraRichiesta, String dataInvio,
                                      String stato, String statoLabel, boolean avviabile) {}
}
