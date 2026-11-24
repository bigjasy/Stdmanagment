package com.example.studentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "registrations", uniqueConstraints = @UniqueConstraint(
        name = "uk_registration_student_course",
        columnNames = {"student_id", "course_id"}))
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    protected Registration() {
    }

    public static Registration validated(Student student, Course course) {
        Registration registration = new Registration();
        registration.student = student;
        registration.course = course;
        registration.status = RegistrationStatus.VALIDATED;
        return registration;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public RegistrationStatus getStatus() {
        return status;
    }
}
