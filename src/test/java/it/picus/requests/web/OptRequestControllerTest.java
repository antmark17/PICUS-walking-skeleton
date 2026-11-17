package it.picus.requests.web;

import it.picus.requests.api.RequestUseCases;
import it.picus.requests.api.RequestView;
import it.picus.requests.domain.RequestStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptRequestControllerTest {

    @Test
    void elencaLeRichiesteRestituiteDalServizioSenzaDatiDemo() {
        RequestUseCases service = mock(RequestUseCases.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<RequestUseCases> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(service);
        UUID id = UUID.randomUUID();
        when(service.listMine()).thenReturn(List.of(new RequestView(
                id, UUID.randomUUID(), "Misura A", RequestStatus.DA_VALUTARE,
                Instant.parse("2026-11-16T10:30:00Z"))));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = new OptRequestController(provider).listMine(model, () -> "opt-a");
        assertEquals("opt/requests", view);
        assertEquals("opt-a", model.get("accountName"));
        @SuppressWarnings("unchecked")
        List<OptRequestController.RequestRow> rows =
                (List<OptRequestController.RequestRow>) model.get("requests");
        assertEquals(1, rows.size());
        assertEquals(id, rows.get(0).id());
        assertEquals("Da valutare", rows.get(0).statoLabel());
        verify(service).listMine();
    }

    @Test
    void senzaImplementazioneGruppo2Risponde503SenzaMockInProduzione() {
        @SuppressWarnings("unchecked")
        ObjectProvider<RequestUseCases> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> new OptRequestController(provider).listMine(new ExtendedModelMap(), () -> "opt-a"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }
}
