package ar.com.ferrodriguez.indexar.web;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler global: ningún endpoint devuelve un stack trace crudo. Errores de negocio
 * mapean a un código HTTP específico; cualquier otra cosa cae en 500 genérico.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SeriesNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SeriesNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage(), Instant.now()));
    }

    public record ErrorResponse(String message, Instant timestamp) {
    }
}
