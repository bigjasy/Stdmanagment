package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.WaitingListEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<WaitingListEntry> findByCourseIdOrderByIdAsc(Long courseId);

    void deleteByCourseId(Long courseId);

    void deleteByStudentId(Long studentId);
}
