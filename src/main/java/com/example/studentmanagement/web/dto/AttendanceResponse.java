package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Attendance;
import com.example.studentmanagement.domain.AttendanceStatus;

public record AttendanceResponse(
        Long id, Long sessionId, Long studentId, AttendanceStatus status) {

    public static AttendanceResponse of(Attendance attendance) {
        return new AttendanceResponse(attendance.getId(), attendance.getSession().getId(),
                attendance.getStudent().getId(), attendance.getStatus());
    }
}
