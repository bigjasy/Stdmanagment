package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Registration;
import com.example.studentmanagement.domain.RegistrationStatus;

public record RegistrationResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseCode,
        RegistrationStatus status) {

    public static RegistrationResponse of(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getStudent().getId(),
                registration.getStudent().getName(),
                registration.getCourse().getId(),
                registration.getCourse().getCode(),
                registration.getStatus());
    }
}
