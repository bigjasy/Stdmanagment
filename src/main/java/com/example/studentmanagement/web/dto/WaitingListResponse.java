package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.WaitingListEntry;

import java.time.LocalDateTime;

public record WaitingListResponse(
        Long id,
        Long courseId,
        Long studentId,
        LocalDateTime requestedAt,
        int position) {

    public static WaitingListResponse of(WaitingListEntry entry, int position) {
        return new WaitingListResponse(entry.getId(), entry.getCourse().getId(), entry.getStudent().getId(),
                entry.getRequestedAt(), position);
    }
}
