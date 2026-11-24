package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.Grade;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.support.TestIds;
import com.example.studentmanagement.web.dto.RegistrationRequest;
import com.example.studentmanagement.web.dto.RegistrationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrations;
    @Mock
    private GradeRepository grades;
    @Mock
    private StudentService students;
    @Mock
    private CourseService courses;

    private RegistrationService service;
    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(LocalDate.of(2026, 11, 24).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new RegistrationService(registrations, grades, students, courses, clock);
        student = TestIds.withId(new Student("Ada", "ada@school.test", "CS"), 1L);
        course = TestIds.withId(new Course("CS101", "Algorithms", 1), 2L);
    }

    @Test
    void createValidatesARegistrationWhenASeatIsFree() {
        when(students.load(1L)).thenReturn(student);
        when(courses.load(2L)).thenReturn(course);
        when(registrations.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(false);
        when(registrations.countByCourseIdAndStatus(2L, RegistrationStatus.VALIDATED)).thenReturn(0L);
        when(registrations.save(any())).thenAnswer(invocation -> TestIds.withId(invocation.getArgument(0), 8L));

        RegistrationResponse response = service.create(new RegistrationRequest(1L, 2L));

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.status()).isEqualTo(RegistrationStatus.VALIDATED);
        assertThat(response.courseCode()).isEqualTo("CS101");
    }

    @Test
    void createRejectsAFullCourse() {
        when(students.load(1L)).thenReturn(student);
        when(courses.load(2L)).thenReturn(course);
        when(registrations.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(false);
        when(registrations.countByCourseIdAndStatus(2L, RegistrationStatus.VALIDATED)).thenReturn(1L);

        assertThatThrownBy(() -> service.create(new RegistrationRequest(1L, 2L)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Course CS101 is full");
    }

    @Test
    void createRejectsADuplicateRegistration() {
        when(students.load(1L)).thenReturn(student);
        when(courses.load(2L)).thenReturn(course);
        when(registrations.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new RegistrationRequest(1L, 2L)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).status())
                .isEqualTo(409);
    }

    @Test
    void createReportsAnUnknownStudentAsABadRequest() {
        when(students.load(9L)).thenThrow(ApiException.notFound("missing"));

        assertThatThrownBy(() -> service.create(new RegistrationRequest(9L, 2L)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Student not found with id 9");
    }

    @Test
    void createRejectsAFailedPrerequisiteFromThePreviousAcademicYear() {
        Course required = TestIds.withId(new Course("CS100", "Intro", 30), 5L);
        course.require(required);
        Exam exam = TestIds.withId(new Exam(required, LocalDate.of(2025, 12, 1)), 7L);
        when(students.load(1L)).thenReturn(student);
        when(courses.load(2L)).thenReturn(course);
        when(grades.findByStudentIdAndExamCourseIdAndExamDateBetween(
                1L, 5L, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(new Grade(exam, student, 8.0)));

        assertThatThrownBy(() -> service.create(new RegistrationRequest(1L, 2L)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Prerequisites not satisfied for course CS101: CS100 (failed)");
    }

    @Test
    void createAcceptsAPassedEquivalentWhenThePrerequisiteWasMissed() {
        Course required = TestIds.withId(new Course("CS100", "Intro", 30), 5L);
        Course equivalent = TestIds.withId(new Course("CS099", "Intro bis", 30), 6L);
        required.acceptEquivalent(equivalent);
        course.require(required);
        Exam exam = TestIds.withId(new Exam(equivalent, LocalDate.of(2026, 1, 15)), 7L);
        when(students.load(1L)).thenReturn(student);
        when(courses.load(2L)).thenReturn(course);
        when(registrations.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(false);
        when(registrations.countByCourseIdAndStatus(2L, RegistrationStatus.VALIDATED)).thenReturn(0L);
        when(registrations.save(any())).thenAnswer(invocation -> TestIds.withId(invocation.getArgument(0), 8L));
        when(grades.findByStudentIdAndExamCourseIdAndExamDateBetween(
                1L, 5L, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of());
        when(grades.findByStudentIdAndExamCourseIdAndExamDateBetween(
                1L, 6L, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(new Grade(exam, student, 14.0)));

        RegistrationResponse response = service.create(new RegistrationRequest(1L, 2L));

        assertThat(response.status()).isEqualTo(RegistrationStatus.VALIDATED);
    }
}
