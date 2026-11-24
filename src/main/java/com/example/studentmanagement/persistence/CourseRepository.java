package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Course> findByRequiredCourses_Id(Long courseId);

    List<Course> findByEquivalentCourses_Id(Long courseId);
}
