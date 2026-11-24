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

import java.time.LocalDate;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "exam_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 20)
    private ExamType type;

    protected Exam() {
    }

    public Exam(Course course, LocalDate date) {
        this(course, date, ExamType.MIDTERM);
    }

    public Exam(Course course, LocalDate date, ExamType type) {
        this.course = course;
        this.date = date;
        this.type = type == null ? ExamType.MIDTERM : type;
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

    public ExamType getType() {
        return type == null ? ExamType.MIDTERM : type;
    }
}
