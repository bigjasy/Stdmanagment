package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.persistence.ExamRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.ExamRequest;
import com.example.studentmanagement.web.dto.ExamResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExamService {

    private final ExamRepository exams;
    private final CourseService courses;

    public ExamService(ExamRepository exams, CourseService courses) {
        this.exams = exams;
        this.courses = courses;
    }

    public ExamResponse create(ExamRequest request) {
        Course course = findCourse(request.courseId());
        Exam saved = exams.save(new Exam(course, request.date(), request.type()));
        return ExamResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> findAll(Long courseId) {
        List<Exam> found = courseId == null ? exams.findAll() : exams.findByCourseId(courseId);
        return found.stream().map(ExamResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public ExamResponse findById(Long id) {
        return ExamResponse.of(load(id));
    }

    public Exam load(Long id) {
        return exams.findById(id)
                .orElseThrow(() -> ApiException.notFound("Exam " + id + " was not found"));
    }

    private Course findCourse(Long id) {
        try {
            return courses.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Course not found with id " + id);
        }
    }
}
