package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
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

    public CourseGradeService(CourseService courses, StudentService students, GradeRepository grades) {
        this.courses = courses;
        this.students = students;
        this.grades = grades;
    }

    public CourseGradeResponse calculate(Long courseId, Long studentId) {
        Course course = courses.load(courseId);
        students.load(studentId);
        WeightedGradeCalculator.Result result = WeightedGradeCalculator.calculate(
                course, studentId, grades.findByStudentIdAndExamCourseId(studentId, courseId));
        if (result.finalGrade() == null && result.appliedWeight() == 0
                && (result.midtermAverage() != null || result.finalAverage() != null || result.projectAverage() != null)) {
            throw ApiException.badRequest("No weight configured for the graded exam categories of course "
                    + course.getCode());
        }
        return CourseGradeResponse.of(result);
    }
}
