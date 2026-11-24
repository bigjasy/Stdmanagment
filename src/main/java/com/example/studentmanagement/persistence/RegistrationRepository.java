package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Registration;
import com.example.studentmanagement.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByCourseIdAndStatus(Long courseId, RegistrationStatus status);

    List<Registration> findByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);

    void deleteByCourseId(Long courseId);
}
