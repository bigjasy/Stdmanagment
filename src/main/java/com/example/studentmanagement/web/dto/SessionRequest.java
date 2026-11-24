package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SessionRequest(
        @NotNull(message = "courseId is required") Long courseId,
        @NotNull(message = "date is required") LocalDate date,
        String label) {
}
