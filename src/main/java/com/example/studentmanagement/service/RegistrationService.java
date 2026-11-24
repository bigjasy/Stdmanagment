package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.AcademicCalendar;
import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.PrerequisitePolicy;
import com.example.studentmanagement.domain.Registration;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.RegistrationRequest;
import com.example.studentmanagement.web.dto.RegistrationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrations;
    private final GradeRepository grades;
    private final StudentService students;
    private final CourseService courses;
    private final Clock clock;

    public RegistrationService(RegistrationRepository registrations,
                               GradeRepository grades,
                               StudentService students,
                               CourseService courses,
                               Clock clock) {
        this.registrations = registrations;
        this.grades = grades;
        this.students = students;
        this.courses = courses;
        this.clock = clock;
    }

    public RegistrationResponse create(RegistrationRequest request) {
        Student student = findStudent(request.studentId());
        Course course = findCourse(request.courseId());
        verifyPrerequisites(course, student);
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

    private void verifyPrerequisites(Course course, Student student) {
        AcademicCalendar.DateRange period = AcademicCalendar.previousYear(LocalDate.now(clock));
        List<String> missing = PrerequisitePolicy.unsatisfied(course, courseId -> grades
                .findByStudentIdAndExamCourseIdAndExamDateBetween(
                        student.getId(), courseId, period.from(), period.to()));
        if (!missing.isEmpty()) {
            throw ApiException.badRequest("Prerequisites not satisfied for course "
                    + course.getCode() + ": " + String.join(", ", missing));
        }
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
