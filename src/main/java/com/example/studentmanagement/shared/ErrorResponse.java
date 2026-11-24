package com.example.studentmanagement.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errors) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, null);
    }

    public ErrorResponse withErrors(Map<String, String> fieldErrors) {
        return new ErrorResponse(timestamp, status, error, message, fieldErrors);
    }
}
