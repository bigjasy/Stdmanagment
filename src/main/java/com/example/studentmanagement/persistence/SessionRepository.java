package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);
}
