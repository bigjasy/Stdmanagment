package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Session;

import java.time.LocalDate;

public record SessionResponse(Long id, Long courseId, String courseCode, LocalDate date, String label) {

    public static SessionResponse of(Session session) {
        return new SessionResponse(session.getId(), session.getCourse().getId(), session.getCourse().getCode(),
                session.getDate(), session.getLabel());
    }
}
