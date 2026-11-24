package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Grade;

public record GradeResponse(Long id, Long examId, Long studentId, Double value) {

    public static GradeResponse of(Grade grade) {
        return new GradeResponse(grade.getId(), grade.getExam().getId(), grade.getStudent().getId(), grade.getValue());
    }
}
