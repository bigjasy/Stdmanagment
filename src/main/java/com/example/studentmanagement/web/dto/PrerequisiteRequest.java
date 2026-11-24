package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.NotNull;

public record PrerequisiteRequest(
        @NotNull(message = "requiredCourseId is required") Long requiredCourseId) {
}
