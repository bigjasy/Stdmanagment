package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Registration;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.RegistrationRequest;
import com.example.studentmanagement.web.dto.RegistrationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrations;
    private final StudentService students;
    private final CourseService courses;

    public RegistrationService(RegistrationRepository registrations,
                               StudentService students,
                               CourseService courses) {
        this.registrations = registrations;
        this.students = students;
        this.courses = courses;
    }

    public RegistrationResponse create(RegistrationRequest request) {
        Student student = findStudent(request.studentId());
        Course course = findCourse(request.courseId());
        rejectDuplicate(student, course);
        long taken = registrations.countByCourseIdAndStatus(course.getId(), RegistrationStatus.VALIDATED);
        if (!course.hasSeatFor(taken)) {
            throw ApiException.badRequest("Course " + course.getCode() + " is full");
        }
        Registration saved = registrations.save(Registration.validated(student, course));
        return RegistrationResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByStudent(Long studentId) {
        students.load(studentId);
        return registrations.findByStudentId(studentId).stream().map(RegistrationResponse::of).toList();
    }

    private Student findStudent(Long id) {
        try {
            return students.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Student not found with id " + id);
        }
    }

    private Course findCourse(Long id) {
        try {
            return courses.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Course not found with id " + id);
        }
    }

    private void rejectDuplicate(Student student, Course course) {
        if (registrations.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw ApiException.conflict("Student " + student.getId()
                    + " is already registered to course " + course.getCode());
        }
    }
}
