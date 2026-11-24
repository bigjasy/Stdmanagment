package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    boolean existsByExamIdAndStudentId(Long examId, Long studentId);

    List<Grade> findByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);

    void deleteByExamCourseId(Long courseId);
}
