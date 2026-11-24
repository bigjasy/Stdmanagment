package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.persistence.AttendanceRepository;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.persistence.StudentRepository;
import com.example.studentmanagement.persistence.NotificationPreferenceRepository;
import com.example.studentmanagement.persistence.NotificationRepository;
import com.example.studentmanagement.persistence.WaitingListRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.StudentRequest;
import com.example.studentmanagement.web.dto.StudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository students;
    private final RegistrationRepository registrations;
    private final GradeRepository grades;
    private final AttendanceRepository attendances;
    private final WaitingListRepository waitingList;
    private final NotificationRepository notifications;
    private final NotificationPreferenceRepository notificationPreferences;

    public StudentService(StudentRepository students,
                          RegistrationRepository registrations,
                          GradeRepository grades,
                          AttendanceRepository attendances,
                          WaitingListRepository waitingList,
                          NotificationRepository notifications,
                          NotificationPreferenceRepository notificationPreferences) {
        this.students = students;
        this.registrations = registrations;
        this.grades = grades;
        this.attendances = attendances;
        this.waitingList = waitingList;
        this.notifications = notifications;
        this.notificationPreferences = notificationPreferences;
    }

    public StudentResponse create(StudentRequest request) {
        rejectDuplicateEmail(request.email(), null);
        Student saved = students.save(new Student(request.name(), request.email(), request.program()));
        return StudentResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return students.findAll().stream().map(StudentResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return StudentResponse.of(load(id));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = load(id);
        rejectDuplicateEmail(request.email(), id);
        student.updateProfile(request.name(), request.email(), request.program(), request.phoneNumber());
        return StudentResponse.of(student);
    }

    public void delete(Long id) {
        load(id);
        attendances.deleteByStudentId(id);
        waitingList.deleteByStudentId(id);
        notifications.deleteByStudentId(id);
        notificationPreferences.deleteByStudentId(id);
        grades.deleteByStudentId(id);
        registrations.deleteByStudentId(id);
        students.deleteById(id);
    }

    public Student load(Long id) {
        return students.findById(id)
                .orElseThrow(() -> ApiException.notFound("Student " + id + " was not found"));
    }

    private void rejectDuplicateEmail(String email, Long currentId) {
        boolean taken = currentId == null
                ? students.existsByEmailIgnoreCase(email)
                : students.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (taken) {
            throw ApiException.conflict("Email already used: " + email);
        }
    }
}
