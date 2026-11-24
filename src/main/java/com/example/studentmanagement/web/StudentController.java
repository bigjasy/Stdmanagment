package com.example.studentmanagement.web;

import com.example.studentmanagement.service.GradeService;
import com.example.studentmanagement.service.NotificationService;
import com.example.studentmanagement.service.RegistrationService;
import com.example.studentmanagement.service.StudentService;
import com.example.studentmanagement.web.dto.GradeResponse;
import com.example.studentmanagement.web.dto.NotificationPreferenceRequest;
import com.example.studentmanagement.web.dto.NotificationPreferenceResponse;
import com.example.studentmanagement.web.dto.NotificationResponse;
import com.example.studentmanagement.web.dto.RegistrationResponse;
import com.example.studentmanagement.web.dto.StudentRequest;
import com.example.studentmanagement.web.dto.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService students;
    private final RegistrationService registrations;
    private final GradeService grades;
    private final NotificationService notifications;

    public StudentController(StudentService students,
                             RegistrationService registrations,
                             GradeService grades,
                             NotificationService notifications) {
        this.students = students;
        this.registrations = registrations;
        this.grades = grades;
        this.notifications = notifications;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return students.create(request);
    }

    @GetMapping
    public List<StudentResponse> findAll() {
        return students.findAll();
    }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable Long id) {
        return students.findById(id);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return students.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        students.delete(id);
    }

    @GetMapping("/{id}/registrations")
    public List<RegistrationResponse> registrations(@PathVariable Long id) {
        return registrations.findByStudent(id);
    }

    @GetMapping("/{id}/grades")
    public List<GradeResponse> grades(@PathVariable Long id) {
        return grades.findByStudent(id);
    }

    @GetMapping("/{id}/notification-preferences")
    public NotificationPreferenceResponse preferences(@PathVariable Long id) {
        return notifications.preference(id);
    }

    @PutMapping("/{id}/notification-preferences")
    public NotificationPreferenceResponse replacePreferences(@PathVariable Long id,
                                                             @RequestBody NotificationPreferenceRequest request) {
        return notifications.replacePreference(id, request);
    }

    @GetMapping("/{id}/notifications")
    public List<NotificationResponse> notifications(@PathVariable Long id) {
        return notifications.history(id);
    }
}
