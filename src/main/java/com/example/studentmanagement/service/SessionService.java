package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Session;
import com.example.studentmanagement.persistence.SessionRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.SessionRequest;
import com.example.studentmanagement.web.dto.SessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessions;
    private final CourseService courses;

    public SessionService(SessionRepository sessions, CourseService courses) {
        this.sessions = sessions;
        this.courses = courses;
    }

    public SessionResponse create(SessionRequest request) {
        Course course = findCourse(request.courseId());
        Session saved = sessions.save(new Session(course, request.date(), request.label()));
        return SessionResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> findAll(Long courseId) {
        List<Session> found = courseId == null ? sessions.findAll() : sessions.findByCourseId(courseId);
        return found.stream().map(SessionResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse findById(Long id) {
        return SessionResponse.of(load(id));
    }

    public Session load(Long id) {
        return sessions.findById(id)
                .orElseThrow(() -> ApiException.notFound("Session " + id + " was not found"));
    }

    private Course findCourse(Long id) {
        try {
            return courses.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Course not found with id " + id);
        }
    }
}
