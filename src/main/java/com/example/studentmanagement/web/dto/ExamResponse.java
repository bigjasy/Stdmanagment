package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.ExamType;

import java.time.LocalDate;

public record ExamResponse(Long id, Long courseId, String courseCode, LocalDate date, ExamType type) {

    public static ExamResponse of(Exam exam) {
        return new ExamResponse(exam.getId(), exam.getCourse().getId(), exam.getCourse().getCode(),
                exam.getDate(), exam.getType());
    }
}
