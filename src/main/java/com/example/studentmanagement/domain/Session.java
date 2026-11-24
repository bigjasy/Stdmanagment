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

import java.time.LocalDate;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "session_date", nullable = false)
    private LocalDate date;

    @Column(length = 120)
    private String label;

    protected Session() {
    }

    public Session(Course course, LocalDate date, String label) {
        this.course = course;
        this.date = date;
        this.label = label == null || label.isBlank() ? null : label.trim();
    }

    public void reschedule(LocalDate date, String label) {
        this.date = date;
        this.label = label == null || label.isBlank() ? null : label.trim();
    }

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLabel() {
        return label;
    }
}
