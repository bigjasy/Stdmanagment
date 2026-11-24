package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.NotificationPreference;

public record NotificationPreferenceResponse(
        Long studentId, boolean emailEnabled, boolean smsEnabled, boolean whatsappEnabled) {

    public static NotificationPreferenceResponse of(NotificationPreference preference) {
        return new NotificationPreferenceResponse(preference.getStudent().getId(),
                preference.isEmailEnabled(), preference.isSmsEnabled(), preference.isWhatsappEnabled());
    }
}
