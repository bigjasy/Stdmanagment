package com.example.studentmanagement.web;

import com.example.studentmanagement.domain.Role;
import com.example.studentmanagement.service.GradeService;
import com.example.studentmanagement.service.NotificationService;
import com.example.studentmanagement.service.RegistrationService;
import com.example.studentmanagement.service.StudentService;
import com.example.studentmanagement.shared.ApiErrorHandler;
import com.example.studentmanagement.web.dto.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService students;
    @Mock
    private RegistrationService registrations;
    @Mock
    private GradeService grades;
    @Mock
    private NotificationService notifications;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new StudentController(students, registrations, grades, notifications))
                .setControllerAdvice(new ApiErrorHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createReturns201() throws Exception {
        when(students.create(any())).thenReturn(
                new StudentResponse(1L, "Ada Lovelace", Role.STUDENT, "ada@school.test", "CS", null));

        mvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada Lovelace\",\"email\":\"ada@school.test\",\"program\":\"CS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ada@school.test"));
    }

    @Test
    void createReturns400WhenTheEmailIsMissing() throws Exception {
        mvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\",\"program\":\"CS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
