package it.picus.requests.web;

import it.picus.requests.application.RequestNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "it.picus.requests.web")
public class RequestWebExceptionHandler {

    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(
        RequestNotFoundException exception) {

        return ResponseEntity.notFound().build();
    }
}
