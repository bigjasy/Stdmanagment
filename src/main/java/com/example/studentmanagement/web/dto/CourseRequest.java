package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseRequest(
        @NotBlank(message = "Code is required") String code,
        @NotBlank(message = "Title is required") String title,
        @NotNull(message = "Capacity is required") @Min(value = 1, message = "Capacity must be at least 1") Integer capacity) {
}
