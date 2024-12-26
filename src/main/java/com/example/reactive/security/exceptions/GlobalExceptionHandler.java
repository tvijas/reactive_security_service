package com.example.reactive.security.exceptions;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Collections.singletonMap(
                        "errors",
                        ex.getBindingResult().getFieldErrors().stream()
                                .collect(Collectors.toMap(
                                        FieldError::getField,
                                        DefaultMessageSourceResolvable::getDefaultMessage
                                ))
                )
        );
    }

    @ExceptionHandler(BasicException.class)
    public ResponseEntity<Map<String, Map<String, String>>> handleValidationExceptions(BasicException ex) {
        return new ResponseEntity<>(Collections.singletonMap("errors", ex.getErrors()), ex.getHttpStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Void> handleValidationExceptions() {
        return ResponseEntity.internalServerError().build();
    }
}
