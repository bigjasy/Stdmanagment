package com.example.studentmanagement.academic;

import com.example.studentmanagement.domain.Student;

/**
 * Academic enrollment check. The implementation owns the transport; this type has no address.
 */
public interface AcademicClient {

    void requireActiveEnrollment(Student student);
}
