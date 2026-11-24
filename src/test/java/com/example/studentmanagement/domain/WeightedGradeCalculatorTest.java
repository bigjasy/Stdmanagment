package com.example.studentmanagement.domain;

import com.example.studentmanagement.support.TestIds;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeightedGradeCalculatorTest {

    @Test
    void standardWeightsCombineCategoryAverages() {
        Course course = TestIds.withId(new Course("CS101", "Algorithms", 20), 1L);
        Student student = TestIds.withId(new Student("Ada", "ada@school.test", "CS"), 2L);
        Exam midterm = new Exam(course, LocalDate.of(2026, 11, 1), ExamType.MIDTERM);
        Exam finale = new Exam(course, LocalDate.of(2026, 12, 20), ExamType.FINAL);
        Exam project = new Exam(course, LocalDate.of(2026, 12, 1), ExamType.PROJECT);

        WeightedGradeCalculator.Result result = WeightedGradeCalculator.calculate(course, 2L, List.of(
                new Grade(midterm, student, 10),
                new Grade(midterm, student, 14),
                new Grade(finale, student, 16),
                new Grade(project, student, 18)));

        assertThat(result.midtermAverage()).isEqualTo(12.0);
        assertThat(result.finalAverage()).isEqualTo(16.0);
        assertThat(result.projectAverage()).isEqualTo(18.0);
        assertThat(result.appliedWeight()).isEqualTo(100);
        assertThat(result.finalGrade()).isEqualTo(15.2);
    }

    @Test
    void missingCategoryIsLeftOutOfTheDivisor() {
        Course course = TestIds.withId(new Course("CS101", "Algorithms", 20), 1L);
        Student student = TestIds.withId(new Student("Ada", "ada@school.test", "CS"), 2L);
        Exam finale = new Exam(course, LocalDate.of(2026, 12, 20), ExamType.FINAL);

        WeightedGradeCalculator.Result result = WeightedGradeCalculator.calculate(
                course, 2L, List.of(new Grade(finale, student, 10)));

        assertThat(result.appliedWeight()).isEqualTo(50);
        assertThat(result.finalGrade()).isEqualTo(10.0);
        assertThat(result.midtermAverage()).isNull();
    }

    @Test
    void nullStoredWeightsFallBackToTheStandardScheme() {
        Course course = TestIds.withId(new Course("CS101", "Algorithms", 20), 1L);
        course.replaceWeights(0, 0, 0);
        setNullWeights(course);

        assertThat(course.midtermWeight()).isEqualTo(30);
        assertThat(course.finalWeight()).isEqualTo(50);
        assertThat(course.projectWeight()).isEqualTo(20);
    }

    private static void setNullWeights(Course course) {
        try {
            for (String name : List.of("midtermWeight", "finalWeight", "projectWeight")) {
                var field = Course.class.getDeclaredField(name);
                field.setAccessible(true);
                field.set(course, null);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
