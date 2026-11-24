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

@Entity
@Table(name = "grades", uniqueConstraints = @UniqueConstraint(
        name = "uk_grade_exam_student",
        columnNames = {"exam_id", "student_id"}))
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "grade_value", nullable = false)
    private Double value;

    protected Grade() {
    }

    public Grade(Exam exam, Student student, double value) {
        this.exam = exam;
        this.student = student;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public Exam getExam() {
        return exam;
    }

    public Student getStudent() {
        return student;
    }

    public Double getValue() {
        return value;
    }
}
