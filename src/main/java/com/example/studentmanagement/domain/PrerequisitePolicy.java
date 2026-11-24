package com.example.studentmanagement.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A required course is satisfied by a passing grade in the previous academic year,
 * or by a passing grade in one of that required course's equivalents.
 */
public final class PrerequisitePolicy {

    public static final double PASS_MARK = 10.0;

    private PrerequisitePolicy() {
    }

    public static List<String> unsatisfied(Course course, GradeHistory history) {
        List<String> missing = new ArrayList<>();
        for (Course required : course.requiredCourses()) {
            Result result = judge(history.gradesFor(required.getId()));
            if (result == Result.PASSED) {
                continue;
            }
            boolean equivalentPassed = required.equivalentCourses().stream()
                    .anyMatch(equivalent -> judge(history.gradesFor(equivalent.getId())) == Result.PASSED);
            if (!equivalentPassed) {
                missing.add(required.getCode() + " (" + result.label() + ")");
            }
        }
        return List.copyOf(missing);
    }

    public static Result judge(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            return Result.ABSENT;
        }
        boolean passed = grades.stream().anyMatch(PrerequisitePolicy::countsAsPassed);
        return passed ? Result.PASSED : Result.FAILED;
    }

    static boolean countsAsPassed(Grade grade) {
        return grade.getValue() != null && grade.getValue() >= PASS_MARK;
    }

    public enum Result {
        PASSED("passed"),
        FAILED("failed"),
        ABSENT("absent");

        private final String label;

        Result(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public interface GradeHistory {
        List<Grade> gradesFor(Long courseId);
    }
}
