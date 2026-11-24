package com.example.studentmanagement.shared;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> api(ApiException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.status());
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ErrorResponse body = ErrorResponse.of(400, "Bad Request", "Validation failed").withErrors(errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request", "Malformed JSON or an invalid value"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
                409, "Conflict", "The operation conflicts with an existing record or a database link"));
    }
}
