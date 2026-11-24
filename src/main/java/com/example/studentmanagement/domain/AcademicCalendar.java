package com.example.studentmanagement.domain;

import java.time.LocalDate;
import java.time.Month;

/** Academic year runs from 1 September through 31 August. */
public final class AcademicCalendar {

    private AcademicCalendar() {
    }

    public static DateRange previousYear(LocalDate today) {
        LocalDate start = startOfYearContaining(today);
        return new DateRange(start.minusYears(1), start.minusDays(1));
    }

    public static LocalDate startOfYearContaining(LocalDate date) {
        int year = date.getMonthValue() >= Month.SEPTEMBER.getValue()
                ? date.getYear()
                : date.getYear() - 1;
        return LocalDate.of(year, Month.SEPTEMBER, 1);
    }

    public record DateRange(LocalDate from, LocalDate to) {
    }
}
