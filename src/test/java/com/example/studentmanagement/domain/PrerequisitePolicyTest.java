package com.example.studentmanagement.domain;

import com.example.studentmanagement.support.TestIds;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrerequisitePolicyTest {

    @Test
    void aGradeBelowTenIsFailedAndAMissingGradeIsAbsent() {
        Student student = TestIds.withId(new Student("Ada", "ada@school.test", "CS"), 1L);
        Course required = TestIds.withId(new Course("CS100", "Intro", 10), 2L);
        Exam exam = new Exam(required, LocalDate.of(2025, 12, 1));

        assertThat(PrerequisitePolicy.judge(List.of(new Grade(exam, student, 9.5))))
                .isEqualTo(PrerequisitePolicy.Result.FAILED);
        assertThat(PrerequisitePolicy.judge(List.of())).isEqualTo(PrerequisitePolicy.Result.ABSENT);
        assertThat(PrerequisitePolicy.judge(List.of(new Grade(exam, student, 10.0))))
                .isEqualTo(PrerequisitePolicy.Result.PASSED);
    }
}
