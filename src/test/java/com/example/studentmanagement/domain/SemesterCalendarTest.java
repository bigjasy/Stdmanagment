package com.example.studentmanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterCalendarTest {

    @Test
    void novemberIsFallRunningThroughJanuary() {
        AcademicCalendar.DateRange range = SemesterCalendar.current(LocalDate.of(2026, 11, 24));

        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(range.to()).isEqualTo(LocalDate.of(2027, 1, 31));
    }

    @Test
    void marchIsSpring() {
        AcademicCalendar.DateRange range = SemesterCalendar.current(LocalDate.of(2026, 3, 2));

        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 8, 31));
    }
}
