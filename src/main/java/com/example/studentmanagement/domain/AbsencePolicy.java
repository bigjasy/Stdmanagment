package com.example.studentmanagement.domain;

import java.util.Optional;

public final class AbsencePolicy {

    public static final int COURSE_LIMIT = 3;
    public static final double SEMESTER_RATE = 0.20;

    private AbsencePolicy() {
    }

    public static Optional<String> courseBlock(String courseCode, long unjustified) {
        if (unjustified >= COURSE_LIMIT) {
            return Optional.of("3 unjustified absences in course " + courseCode
                    + " - pending administration decision");
        }
        return Optional.empty();
    }

    public static Optional<String> semesterBlock(long unjustified, long total) {
        if (total == 0) {
            return Optional.empty();
        }
        double rate = (double) unjustified / total;
        if (rate >= SEMESTER_RATE) {
            return Optional.of("Semester absence rate " + Math.round(rate * 100)
                    + "% (limit 20%) - pending administration decision");
        }
        return Optional.empty();
    }
}
