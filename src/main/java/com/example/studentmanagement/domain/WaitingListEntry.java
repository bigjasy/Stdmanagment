package com.example.studentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_list", uniqueConstraints = @UniqueConstraint(
        name = "uk_waiting_list_student_course",
        columnNames = {"student_id", "course_id"}))
public class WaitingListEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    protected WaitingListEntry() {
    }

    public WaitingListEntry(Course course, Student student, LocalDateTime requestedAt) {
        this.course = course;
        this.student = student;
        this.requestedAt = requestedAt;
    }

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
