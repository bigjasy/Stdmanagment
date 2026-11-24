package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.service.AbsenceRuleService;
import com.example.studentmanagement.service.NotificationService;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.support.TestIds;
import com.example.studentmanagement.web.dto.GradeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository grades;
    @Mock
    private ExamService exams;
    @Mock
    private StudentService students;
    @Mock
    private AbsenceRuleService absenceRules;
    @Mock
    private NotificationService notifications;

    private GradeService service;

    @BeforeEach
    void setUp() {
        service = new GradeService(grades, exams, students, absenceRules, notifications);
    }

    @Test
    void createRejectsASecondGradeForTheSameExam() {
        Student student = TestIds.withId(new Student("Ada", "ada@school.test", "CS"), 1L);
        Course course = TestIds.withId(new Course("CS101", "Algorithms", 10), 2L);
        Exam exam = TestIds.withId(new Exam(course, LocalDate.of(2026, 12, 20)), 3L);
        when(exams.load(3L)).thenReturn(exam);
        when(students.load(1L)).thenReturn(student);
        when(grades.existsByExamIdAndStudentId(3L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new GradeRequest(3L, 1L, 15.0)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).status())
                .isEqualTo(409);
    }

    @Test
    void createReportsAnUnknownExamAsABadRequest() {
        when(exams.load(3L)).thenThrow(ApiException.notFound("missing"));

        assertThatThrownBy(() -> service.create(new GradeRequest(3L, 1L, 15.0)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Exam not found with id 3");
    }
}
