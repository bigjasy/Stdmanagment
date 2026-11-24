package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.exam.ExamCatalog;
import com.example.studentmanagement.exam.ExamMirror;
import com.example.studentmanagement.persistence.CourseRepository;
import com.example.studentmanagement.persistence.ExamRepository;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.support.TestIds;
import com.example.studentmanagement.web.dto.ExamResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamCatalogFallbackTest {

    @Mock
    private ExamRepository exams;
    @Mock
    private GradeRepository grades;
    @Mock
    private CourseService courses;
    @Mock
    private ExamCatalog catalog;
    @Mock
    private CourseRepository courseRepository;

    private ExamService service;

    @BeforeEach
    void setUp() {
        service = new ExamService(exams, grades, courses, catalog, new ExamMirror(courseRepository));
    }

    @Test
    void aReadFallsBackToTheLocalMirrorWhenTheExternalServiceIsDown() {
        Exam local = TestIds.withId(new Exam(
                TestIds.withId(new Course("CS101", "Algorithms", 10), 1L),
                LocalDate.of(2026, 12, 20)), 4L);
        when(catalog.list()).thenThrow(ApiException.unavailable("down"));
        when(exams.findAll()).thenReturn(List.of(local));

        List<ExamResponse> responses = service.findAll(null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(4L);
        assertThat(responses.get(0).externalId()).isNull();
    }
}
