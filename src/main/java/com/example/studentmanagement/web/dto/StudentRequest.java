package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
        @NotBlank(message = "Program is required") String program) {
}
