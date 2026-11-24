package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
        @NotNull(message = "sessionId is required") Long sessionId,
        @NotNull(message = "studentId is required") Long studentId,
        @NotNull(message = "status is required") AttendanceStatus status) {
}
