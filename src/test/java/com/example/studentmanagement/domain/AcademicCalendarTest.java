package com.example.studentmanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AcademicCalendarTest {

    @Test
    void previousYearFromNovemberStartsTheSeptemberBeforeLast() {
        AcademicCalendar.DateRange range = AcademicCalendar.previousYear(LocalDate.of(2026, 11, 24));

        assertThat(range.from()).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 8, 31));
    }

    @Test
    void previousYearFromJanuaryStillBelongsToTheYearThatStartedLastSeptember() {
        AcademicCalendar.DateRange range = AcademicCalendar.previousYear(LocalDate.of(2026, 1, 15));

        assertThat(range.from()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(range.to()).isEqualTo(LocalDate.of(2025, 8, 31));
    }

    @Test
    void septemberFirstOpensANewAcademicYear() {
        assertThat(AcademicCalendar.startOfYearContaining(LocalDate.of(2026, 9, 1)))
                .isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(AcademicCalendar.startOfYearContaining(LocalDate.of(2026, 8, 31)))
                .isEqualTo(LocalDate.of(2025, 9, 1));
    }
}
