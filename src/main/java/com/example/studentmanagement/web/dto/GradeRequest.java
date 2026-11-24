package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record GradeRequest(
        @NotNull(message = "examId is required") Long examId,
        @NotNull(message = "studentId is required") Long studentId,
        @NotNull(message = "value is required")
        @DecimalMin(value = "0.0", message = "Grade must be >= 0")
        @DecimalMax(value = "20.0", message = "Grade must be <= 20") Double value) {
}
