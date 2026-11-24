package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);
}
