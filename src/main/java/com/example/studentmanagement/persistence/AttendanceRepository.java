package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Attendance;
import com.example.studentmanagement.domain.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsBySessionIdAndStudentId(Long sessionId, Long studentId);

    List<Attendance> findByStudentId(Long studentId);

    long countByStudentIdAndSessionCourseIdAndStatusAndSessionDateBetween(
            Long studentId, Long courseId, AttendanceStatus status, LocalDate from, LocalDate to);

    long countByStudentIdAndStatusAndSessionDateBetween(
            Long studentId, AttendanceStatus status, LocalDate from, LocalDate to);

    long countByStudentIdAndSessionDateBetween(Long studentId, LocalDate from, LocalDate to);

    void deleteByStudentId(Long studentId);

    void deleteBySessionCourseId(Long courseId);
}
