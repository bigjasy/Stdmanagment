package com.example.studentmanagement.exam;

public record RemoteExam(
        String externalId,
        String courseCode,
        String examDate,
        String category,
        String status) {
}
