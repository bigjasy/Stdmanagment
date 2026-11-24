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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private GradeStatus status;

    @Column(name = "block_reason", length = 255)
    private String blockReason;

    protected Grade() {
    }

    public Grade(Exam exam, Student student, double value) {
        this.exam = exam;
        this.student = student;
        this.value = value;
        this.status = GradeStatus.VALIDATED;
    }

    public void holdForAdministration(String reason) {
        this.status = GradeStatus.PENDING_ADMIN_DECISION;
        this.blockReason = reason;
    }

    public GradeStatus status() {
        return status == null ? GradeStatus.VALIDATED : status;
    }

    public boolean countsAsPassed(double passMark) {
        return status() == GradeStatus.VALIDATED && value != null && value >= passMark;
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

    public String getBlockReason() {
        return blockReason;
    }
}
