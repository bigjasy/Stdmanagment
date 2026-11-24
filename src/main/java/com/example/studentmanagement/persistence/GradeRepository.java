package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    boolean existsByExamIdAndStudentId(Long examId, Long studentId);

    boolean existsByExamId(Long examId);

    List<Grade> findByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);

    void deleteByExamCourseId(Long courseId);

    List<Grade> findByStudentIdAndExamCourseIdAndExamDateBetween(
            Long studentId, Long courseId, LocalDate from, LocalDate to);

    List<Grade> findByStudentIdAndExamCourseId(Long studentId, Long courseId);
}
