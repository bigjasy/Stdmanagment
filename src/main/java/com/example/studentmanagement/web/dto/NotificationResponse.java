package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Notification;
import com.example.studentmanagement.domain.NotificationChannel;
import com.example.studentmanagement.domain.NotificationStatus;
import com.example.studentmanagement.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        NotificationChannel channel,
        NotificationStatus status,
        String message,
        String recipient,
        Long referenceId,
        String failureReason,
        LocalDateTime createdAt) {

    public static NotificationResponse of(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getChannel(),
                notification.getStatus(), notification.getMessage(), notification.getRecipient(),
                notification.getReferenceId(), notification.getFailureReason(), notification.getCreatedAt());
    }
}
