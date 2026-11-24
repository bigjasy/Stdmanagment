package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCourseId(Long courseId);

    java.util.Optional<Exam> findByExternalId(String externalId);

    void deleteByCourseId(Long courseId);
}
