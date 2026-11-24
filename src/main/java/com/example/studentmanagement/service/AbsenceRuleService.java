package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.AbsencePolicy;
import com.example.studentmanagement.domain.AcademicCalendar;
import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.SemesterCalendar;
import com.example.studentmanagement.persistence.AttendanceRepository;
import com.example.studentmanagement.domain.AttendanceStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AbsenceRuleService {

    private final AttendanceRepository attendances;
    private final Clock clock;

    public AbsenceRuleService(AttendanceRepository attendances, Clock clock) {
        this.attendances = attendances;
        this.clock = clock;
    }

    public Optional<String> courseBlock(Long studentId, Course course) {
        AcademicCalendar.DateRange semester = SemesterCalendar.current(LocalDate.now(clock));
        long unjustified = attendances.countByStudentIdAndSessionCourseIdAndStatusAndSessionDateBetween(
                studentId, course.getId(), AttendanceStatus.ABSENT_UNJUSTIFIED, semester.from(), semester.to());
        return AbsencePolicy.courseBlock(course.getCode(), unjustified);
    }

    public Optional<String> semesterBlock(Long studentId) {
        AcademicCalendar.DateRange semester = SemesterCalendar.current(LocalDate.now(clock));
        long unjustified = attendances.countByStudentIdAndStatusAndSessionDateBetween(
                studentId, AttendanceStatus.ABSENT_UNJUSTIFIED, semester.from(), semester.to());
        long total = attendances.countByStudentIdAndSessionDateBetween(studentId, semester.from(), semester.to());
        return AbsencePolicy.semesterBlock(unjustified, total);
    }
}
