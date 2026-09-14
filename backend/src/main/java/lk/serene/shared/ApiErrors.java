package lk.serene.shared;

import java.util.Map;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("message", e.getReason() == null ? "Request rejected" : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "message",
                e.getBindingResult().getFieldErrors().stream()
                    .map(x -> x.getField() + ": " + x.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid input")));
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class
  })
  ResponseEntity<?> conflict(Exception e) {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "message",
                "A conflicting record exists or this record changed. Refresh before retrying."));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<?> json(Exception e) {
    return ResponseEntity.badRequest().body(Map.of("message", "Invalid request fields or values"));
  }
}
