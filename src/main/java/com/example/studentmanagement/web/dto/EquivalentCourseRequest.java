package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.NotNull;

public record EquivalentCourseRequest(
        @NotNull(message = "equivalentCourseId is required") Long equivalentCourseId) {
}
