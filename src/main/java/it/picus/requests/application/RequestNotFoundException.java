package it.picus.requests.application;

import java.util.UUID;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(UUID id) {
        super("Richiesta non trovata: " + id);
    }
}
