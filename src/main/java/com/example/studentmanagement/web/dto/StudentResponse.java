package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Role;
import com.example.studentmanagement.domain.Student;

public record StudentResponse(Long id, String name, Role role, String email, String program) {

    public static StudentResponse of(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getRole(),
                student.getEmail(), student.getProgram());
    }
}
