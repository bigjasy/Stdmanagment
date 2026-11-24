package com.example.studentmanagement.exam;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.ExamStatus;
import com.example.studentmanagement.domain.ExamType;
import com.example.studentmanagement.persistence.CourseRepository;
import com.example.studentmanagement.support.TestIds;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamMirrorTest {

    @Mock
    private CourseRepository courses;

    @Test
    void mapsExternalStatusAndCategory() {
        Course course = TestIds.withId(new Course("CS101", "Algorithms", 20), 1L);
        when(courses.findByCodeIgnoreCase("CS101")).thenReturn(Optional.of(course));
        ExamMirror mirror = new ExamMirror(courses);

        Exam exam = mirror.create(new RemoteExam("ext-1", "CS101", "2026-12-20", "FINAL", "PUBLISHED"));

        assertThat(exam.getType()).isEqualTo(ExamType.FINAL);
        assertThat(exam.getStatus()).isEqualTo(ExamStatus.ACTIVE);
        assertThat(exam.getExternalId()).isEqualTo("ext-1");
        assertThat(exam.getDate()).isEqualTo(LocalDate.of(2026, 12, 20));
    }

    @Test
    void aGradedMirrorKeepsItsDateAndCategory() {
        Course course = TestIds.withId(new Course("CS101", "Algorithms", 20), 1L);
        Exam exam = new Exam(course, LocalDate.of(2026, 1, 10), ExamType.MIDTERM);
        ExamMirror mirror = new ExamMirror(courses);

        mirror.apply(exam, new RemoteExam("ext-1", "CS101", "2026-12-20", "FINAL", "CANCELED"), true);

        assertThat(exam.getDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(exam.getType()).isEqualTo(ExamType.MIDTERM);
        assertThat(exam.getStatus()).isEqualTo(ExamStatus.CANCELLED);
        assertThat(exam.getExternalId()).isEqualTo("ext-1");
    }
}
