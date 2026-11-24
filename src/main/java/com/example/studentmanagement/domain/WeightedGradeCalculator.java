package com.example.studentmanagement.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Averages each exam category, then applies only the weights of categories that have a grade. */
public final class WeightedGradeCalculator {

    private WeightedGradeCalculator() {
    }

    public static Result calculate(Course course, Long studentId, List<Grade> grades) {
        Map<ExamType, List<Double>> byType = new EnumMap<>(ExamType.class);
        for (Grade grade : grades) {
            if (grade.getValue() == null || grade.getExam() == null) {
                continue;
            }
            byType.computeIfAbsent(grade.getExam().getType(), type -> new ArrayList<>()).add(grade.getValue());
        }

        Double midterm = average(byType.get(ExamType.MIDTERM));
        Double finale = average(byType.get(ExamType.FINAL));
        Double project = average(byType.get(ExamType.PROJECT));
        Part midtermPart = part(midterm, course.midtermWeight());
        Part finalPart = part(finale, course.finalWeight());
        Part projectPart = part(project, course.projectWeight());
        double weighted = midtermPart.sum() + finalPart.sum() + projectPart.sum();
        int applied = midtermPart.weight() + finalPart.weight() + projectPart.weight();
        Double finalGrade = applied == 0 ? null : round(weighted / applied);

        return new Result(course.getId(), course.getCode(), studentId,
                midterm, finale, project,
                course.midtermWeight(), course.finalWeight(), course.projectWeight(),
                applied, finalGrade);
    }

    private static Part part(Double average, int weight) {
        if (average == null) {
            return new Part(0, 0);
        }
        return new Part(average * weight, weight);
    }

    private static Double average(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        double sum = 0;
        for (Double value : values) {
            sum += value;
        }
        return round(sum / values.size());
    }

    static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record Part(double sum, int weight) {
    }

    public record Result(
            Long courseId,
            String courseCode,
            Long studentId,
            Double midtermAverage,
            Double finalAverage,
            Double projectAverage,
            int midtermWeight,
            int finalWeight,
            int projectWeight,
            int appliedWeight,
            Double finalGrade) {
    }
}
