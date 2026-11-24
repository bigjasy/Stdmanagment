package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.ExamType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ExamRequest(
        @NotNull(message = "courseId is required") Long courseId,
        @NotNull(message = "date is required") LocalDate date,
        ExamType type) {

    public ExamRequest {
        if (type == null) {
            type = ExamType.MIDTERM;
        }
    }
}
