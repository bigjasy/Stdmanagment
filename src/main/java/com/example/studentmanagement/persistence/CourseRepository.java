package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
