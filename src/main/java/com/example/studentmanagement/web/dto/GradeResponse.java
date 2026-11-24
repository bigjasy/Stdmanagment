package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Grade;
import com.example.studentmanagement.domain.GradeStatus;

public record GradeResponse(Long id, Long examId, Long studentId, Double value, GradeStatus status, String blockReason) {

    public static GradeResponse of(Grade grade) {
        return new GradeResponse(grade.getId(), grade.getExam().getId(), grade.getStudent().getId(),
                grade.getValue(), grade.status(), grade.getBlockReason());
    }
}
