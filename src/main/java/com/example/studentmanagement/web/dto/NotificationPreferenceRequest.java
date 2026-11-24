package com.example.studentmanagement.web.dto;

public record NotificationPreferenceRequest(
        boolean emailEnabled,
        boolean smsEnabled,
        boolean whatsappEnabled) {
}
