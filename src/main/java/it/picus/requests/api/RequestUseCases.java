package it.picus.requests.api;

import java.util.List;
import java.util.UUID;

/** Contratto concordato tra Requests (G2) e web MVC (G3). */
public interface RequestUseCases {
    UUID submitRequest(String misuraRichiesta);
    List<RequestView> listMine();
    RequestView getMine(UUID id);
    List<RequestView> listForBanco();
    RequestView getForBanco(UUID id);
    void startProcessing(UUID id);
}
