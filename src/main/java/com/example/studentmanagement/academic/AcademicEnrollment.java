package com.example.studentmanagement.academic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AcademicEnrollment(Long studentId, boolean active, String program) {
}
