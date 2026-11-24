package com.example.studentmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbsencePolicyTest {

    @Test
    void threeUnjustifiedAbsencesBlockTheCourse() {
        assertThat(AbsencePolicy.courseBlock("CS101", 2)).isEmpty();
        assertThat(AbsencePolicy.courseBlock("CS101", 3)).contains(
                "3 unjustified absences in course CS101 - pending administration decision");
    }

    @Test
    void aTwentyPercentSemesterRateBlocksFinals() {
        assertThat(AbsencePolicy.semesterBlock(1, 10)).isEmpty();
        assertThat(AbsencePolicy.semesterBlock(2, 10).orElse("")).contains("20%");
        assertThat(AbsencePolicy.semesterBlock(0, 0)).isEmpty();
    }

    @Test
    void aBlockedGradeDoesNotCountAsPassed() {
        Student student = com.example.studentmanagement.support.TestIds.withId(
                new Student("Ada", "ada@school.test", "CS"), 1L);
        Course course = com.example.studentmanagement.support.TestIds.withId(new Course("CS100", "Intro", 10), 2L);
        Grade grade = new Grade(new Exam(course, java.time.LocalDate.of(2025, 12, 1)), student, 16);
        grade.holdForAdministration("blocked");

        assertThat(PrerequisitePolicy.judge(java.util.List.of(grade))).isEqualTo(PrerequisitePolicy.Result.FAILED);
    }
}
