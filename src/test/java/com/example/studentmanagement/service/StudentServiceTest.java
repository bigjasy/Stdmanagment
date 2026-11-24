package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.persistence.StudentRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.support.TestIds;
import com.example.studentmanagement.web.dto.StudentRequest;
import com.example.studentmanagement.web.dto.StudentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository students;
    @Mock
    private RegistrationRepository registrations;
    @Mock
    private GradeRepository grades;
    @InjectMocks
    private StudentService service;

    @Test
    void createStoresAStudentAndNormalizesTheEmail() {
        when(students.existsByEmailIgnoreCase("Ada@School.test")).thenReturn(false);
        when(students.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            return TestIds.withId(student, 4L);
        });

        StudentResponse response = service.create(new StudentRequest("Ada Lovelace", "Ada@School.test", "CS"));

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.email()).isEqualTo("ada@school.test");
        assertThat(response.role()).isEqualTo(com.example.studentmanagement.domain.Role.STUDENT);
    }

    @Test
    void createRejectsADuplicateEmail() {
        when(students.existsByEmailIgnoreCase("ada@school.test")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new StudentRequest("Ada", "ada@school.test", "CS")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Email already used");
    }

    @Test
    void deleteRemovesGradesAndRegistrationsBeforeTheStudent() {
        when(students.findById(4L)).thenReturn(Optional.of(TestIds.withId(new Student("Ada", "ada@school.test", "CS"), 4L)));

        service.delete(4L);

        verify(grades).deleteByStudentId(4L);
        verify(registrations).deleteByStudentId(4L);
        verify(students).deleteById(4L);
    }

    @Test
    void findByIdReturns404WhenMissing() {
        when(students.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).status())
                .isEqualTo(404);
    }
}
