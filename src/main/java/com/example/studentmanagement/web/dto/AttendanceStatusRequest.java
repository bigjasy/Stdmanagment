package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.AttendanceStatus;
import com.example.studentmanagement.domain.Role;
import jakarta.validation.constraints.NotNull;

public record AttendanceStatusRequest(
        @NotNull(message = "role is required") Role role,
        @NotNull(message = "status is required") AttendanceStatus status) {
}
