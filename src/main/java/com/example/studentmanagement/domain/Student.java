package com.example.studentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "id")
public class Student extends User {

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String program;

    protected Student() {
    }

    public Student(String name, String email, String program) {
        super(name, Role.STUDENT);
        this.email = email.trim().toLowerCase();
        this.program = program.trim();
    }

    public void updateProfile(String name, String email, String program) {
        rename(name);
        this.email = email.trim().toLowerCase();
        this.program = program.trim();
    }

    public String getEmail() {
        return email;
    }

    public String getProgram() {
        return program;
    }
}
