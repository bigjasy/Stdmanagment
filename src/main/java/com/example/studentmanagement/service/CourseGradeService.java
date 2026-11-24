package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.GradeStatus;
import com.example.studentmanagement.domain.WeightedGradeCalculator;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.CourseGradeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseGradeService {

    private final CourseService courses;
    private final StudentService students;
    private final GradeRepository grades;
    private final AbsenceRuleService absenceRules;

    public CourseGradeService(CourseService courses, StudentService students, GradeRepository grades,
                              AbsenceRuleService absenceRules) {
        this.courses = courses;
        this.students = students;
        this.grades = grades;
        this.absenceRules = absenceRules;
    }

    public CourseGradeResponse calculate(Long courseId, Long studentId) {
        Course course = courses.load(courseId);
        students.load(studentId);
        WeightedGradeCalculator.Result result = WeightedGradeCalculator.calculate(
                course, studentId, grades.findByStudentIdAndExamCourseId(studentId, courseId));
        String reason = absenceRules.courseBlock(studentId, course)
                .or(() -> absenceRules.semesterBlock(studentId))
                .orElse(null);
        GradeStatus status = reason == null ? GradeStatus.VALIDATED : GradeStatus.PENDING_ADMIN_DECISION;
        if (result.finalGrade() == null && result.appliedWeight() == 0
                && status == GradeStatus.VALIDATED
                && (result.midtermAverage() != null || result.finalAverage() != null || result.projectAverage() != null)) {
            throw ApiException.badRequest("No weight configured for the graded exam categories of course "
                    + course.getCode());
        }
        return CourseGradeResponse.of(result, status, reason);
    }
}
