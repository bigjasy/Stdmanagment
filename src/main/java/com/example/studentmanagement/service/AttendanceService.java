package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Attendance;
import com.example.studentmanagement.domain.Role;
import com.example.studentmanagement.domain.Session;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.persistence.AttendanceRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.AttendanceRequest;
import com.example.studentmanagement.web.dto.AttendanceResponse;
import com.example.studentmanagement.web.dto.AttendanceStatusRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendances;
    private final SessionService sessions;
    private final StudentService students;

    public AttendanceService(AttendanceRepository attendances, SessionService sessions, StudentService students) {
        this.attendances = attendances;
        this.sessions = sessions;
        this.students = students;
    }

    public AttendanceResponse create(AttendanceRequest request) {
        Session session = findSession(request.sessionId());
        Student student = findStudent(request.studentId());
        if (attendances.existsBySessionIdAndStudentId(session.getId(), student.getId())) {
            throw ApiException.conflict("Attendance already recorded for student " + student.getId()
                    + " in session " + session.getId());
        }
        Attendance saved = attendances.save(new Attendance(session, student, request.status()));
        return AttendanceResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> findAll(Long studentId) {
        List<Attendance> found = studentId == null ? attendances.findAll() : attendances.findByStudentId(studentId);
        return found.stream().map(AttendanceResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public AttendanceResponse findById(Long id) {
        return AttendanceResponse.of(load(id));
    }

    public AttendanceResponse updateStatus(Long id, AttendanceStatusRequest request) {
        if (request.role() == Role.STUDENT) {
            throw ApiException.forbidden("Only an admin or a teacher can change an attendance status");
        }
        Attendance attendance = load(id);
        attendance.changeStatus(request.status());
        return AttendanceResponse.of(attendance);
    }

    private Attendance load(Long id) {
        return attendances.findById(id)
                .orElseThrow(() -> ApiException.notFound("Attendance " + id + " was not found"));
    }

    private Session findSession(Long id) {
        try {
            return sessions.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Session not found with id " + id);
        }
    }

    private Student findStudent(Long id) {
        try {
            return students.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Student not found with id " + id);
        }
    }
}
