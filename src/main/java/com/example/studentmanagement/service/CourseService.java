package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.persistence.CourseRepository;
import com.example.studentmanagement.persistence.ExamRepository;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.CourseRequest;
import com.example.studentmanagement.web.dto.CourseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courses;
    private final RegistrationRepository registrations;
    private final ExamRepository exams;
    private final GradeRepository grades;

    public CourseService(CourseRepository courses,
                         RegistrationRepository registrations,
                         ExamRepository exams,
                         GradeRepository grades) {
        this.courses = courses;
        this.registrations = registrations;
        this.exams = exams;
        this.grades = grades;
    }

    public CourseResponse create(CourseRequest request) {
        rejectDuplicateCode(request.code(), null);
        Course saved = courses.save(new Course(request.code(), request.title(), request.capacity()));
        return CourseResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courses.findAll().stream().map(CourseResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return CourseResponse.of(load(id));
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course course = load(id);
        rejectDuplicateCode(request.code(), id);
        long taken = registrations.countByCourseIdAndStatus(id, RegistrationStatus.VALIDATED);
        if (request.capacity() < taken) {
            throw ApiException.badRequest(
                    "Capacity cannot be lower than the " + taken + " validated registrations");
        }
        course.revise(request.code(), request.title(), request.capacity());
        return CourseResponse.of(course);
    }

    public void delete(Long id) {
        load(id);
        grades.deleteByExamCourseId(id);
        exams.deleteByCourseId(id);
        registrations.deleteByCourseId(id);
        courses.deleteById(id);
    }

    public Course load(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> ApiException.notFound("Course " + id + " was not found"));
    }

    private void rejectDuplicateCode(String code, Long currentId) {
        boolean taken = currentId == null
                ? courses.existsByCodeIgnoreCase(code)
                : courses.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (taken) {
            throw ApiException.conflict("Course code already used: " + code);
        }
    }
}
