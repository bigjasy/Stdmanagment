package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Exam;

import java.time.LocalDate;

public record ExamResponse(Long id, Long courseId, String courseCode, LocalDate date) {

    public static ExamResponse of(Exam exam) {
        return new ExamResponse(exam.getId(), exam.getCourse().getId(), exam.getCourse().getCode(), exam.getDate());
    }
}
