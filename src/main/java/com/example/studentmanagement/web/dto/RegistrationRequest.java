package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.NotNull;

public record RegistrationRequest(
        @NotNull(message = "studentId is required") Long studentId,
        @NotNull(message = "courseId is required") Long courseId) {
}
