package tn.esprit.autoloc.web.controller;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.autoloc.exception.ResourceNotFoundException;

/**
 * Traduit les exceptions des services en réponses HTTP : les contrôleurs
 * ne produisent eux-mêmes ni 404 ni 400.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErreurApi(int statut, String message, LocalDateTime horodatage) {
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErreurApi> handleNotFound(ResourceNotFoundException e) {
        return construire(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErreurApi> handleBadRequest(IllegalArgumentException e) {
        return construire(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ResponseEntity<ErreurApi> construire(HttpStatus statut, String message) {
        return ResponseEntity.status(statut)
                .body(new ErreurApi(statut.value(), message, LocalDateTime.now()));
    }
}
