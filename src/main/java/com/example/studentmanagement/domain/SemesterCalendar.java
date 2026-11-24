package com.example.studentmanagement.domain;

import java.time.LocalDate;
import java.time.Month;

/** Fall runs September through January. Spring runs February through August. */
public final class SemesterCalendar {

    private SemesterCalendar() {
    }

    public static AcademicCalendar.DateRange current(LocalDate today) {
        Month month = today.getMonth();
        if (month == Month.JANUARY || month.getValue() >= Month.SEPTEMBER.getValue()) {
            int startYear = month == Month.JANUARY ? today.getYear() - 1 : today.getYear();
            return new AcademicCalendar.DateRange(
                    LocalDate.of(startYear, Month.SEPTEMBER, 1),
                    LocalDate.of(startYear + 1, Month.JANUARY, 31));
        }
        return new AcademicCalendar.DateRange(
                LocalDate.of(today.getYear(), Month.FEBRUARY, 1),
                LocalDate.of(today.getYear(), Month.AUGUST, 31));
    }
}
