package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.GradeStatus;
import com.example.studentmanagement.domain.WeightedGradeCalculator;

public record CourseGradeResponse(
        Long courseId,
        String courseCode,
        Long studentId,
        Double midtermAverage,
        Double finalAverage,
        Double projectAverage,
        int midtermWeight,
        int finalWeight,
        int projectWeight,
        int appliedWeight,
        Double finalGrade,
        GradeStatus status,
        String blockReason) {

    public static CourseGradeResponse of(WeightedGradeCalculator.Result result, GradeStatus status, String blockReason) {
        Double finalGrade = status == GradeStatus.PENDING_ADMIN_DECISION ? null : result.finalGrade();
        return new CourseGradeResponse(result.courseId(), result.courseCode(), result.studentId(),
                result.midtermAverage(), result.finalAverage(), result.projectAverage(),
                result.midtermWeight(), result.finalWeight(), result.projectWeight(),
                result.appliedWeight(), finalGrade, status, blockReason);
    }
}
