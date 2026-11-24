package com.example.studentmanagement.persistence;

import com.example.studentmanagement.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
