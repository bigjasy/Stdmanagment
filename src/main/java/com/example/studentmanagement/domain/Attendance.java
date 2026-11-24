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
@Table(name = "attendances", uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_session_student",
        columnNames = {"session_id", "student_id"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttendanceStatus status;

    protected Attendance() {
    }

    public Attendance(Session session, Student student, AttendanceStatus status) {
        this.session = session;
        this.student = student;
        this.status = status;
    }

    public void changeStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public Student getStudent() {
        return student;
    }

    public AttendanceStatus getStatus() {
        return status;
    }
}
