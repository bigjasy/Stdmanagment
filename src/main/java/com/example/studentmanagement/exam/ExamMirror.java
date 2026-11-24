package com.example.studentmanagement.exam;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.ExamStatus;
import com.example.studentmanagement.domain.ExamType;
import com.example.studentmanagement.persistence.CourseRepository;
import com.example.studentmanagement.shared.ApiException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;

@Component
public class ExamMirror {

    private final CourseRepository courses;

    public ExamMirror(CourseRepository courses) {
        this.courses = courses;
    }

    public Exam create(RemoteExam remote) {
        return apply(Exam.blank(), remote, false);
    }

    public Exam apply(Exam target, RemoteExam remote, boolean graded) {
        if (remote.externalId() == null || remote.externalId().isBlank()) {
            throw ApiException.badGateway("External exam has no identifier");
        }
        target.link(remote.externalId(), toStatus(remote.status()));
        if (!graded) {
            target.refresh(resolveCourse(remote), toDate(remote), toType(remote.category()));
        }
        return target;
    }

    public ExamType toType(String category) {
        if (category == null || category.isBlank()) {
            return ExamType.MIDTERM;
        }
        return switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "FINAL", "EXAM_FINAL" -> ExamType.FINAL;
            case "PROJECT", "ASSIGNMENT" -> ExamType.PROJECT;
            default -> ExamType.MIDTERM;
        };
    }

    public ExamStatus toStatus(String status) {
        if (status == null || status.isBlank()) {
            return ExamStatus.UNKNOWN;
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "ACTIVE", "SCHEDULED", "PUBLISHED", "DRAFT", "OPEN" -> ExamStatus.ACTIVE;
            case "CANCELLED", "CANCELED", "VOID", "REMOVED" -> ExamStatus.CANCELLED;
            default -> ExamStatus.UNKNOWN;
        };
    }

    private Course resolveCourse(RemoteExam remote) {
        if (remote.courseCode() == null || remote.courseCode().isBlank()) {
            throw ApiException.badGateway("External exam " + remote.externalId() + " has no course code");
        }
        return courses.findByCodeIgnoreCase(remote.courseCode())
                .orElseThrow(() -> ApiException.badGateway(
                        "External exam " + remote.externalId() + " refers to unknown course " + remote.courseCode()));
    }

    private LocalDate toDate(RemoteExam remote) {
        if (remote.examDate() == null || remote.examDate().isBlank()) {
            throw ApiException.badGateway("External exam " + remote.externalId() + " has no date");
        }
        try {
            return LocalDate.parse(remote.examDate().trim());
        } catch (RuntimeException exception) {
            throw ApiException.badGateway("External exam " + remote.externalId() + " has an invalid date");
        }
    }
}
