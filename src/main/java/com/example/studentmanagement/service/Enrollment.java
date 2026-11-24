package com.example.studentmanagement.service;

import com.example.studentmanagement.web.dto.RegistrationResponse;
import com.example.studentmanagement.web.dto.WaitingListResponse;

public record Enrollment(RegistrationResponse registration, WaitingListResponse waiting) {

    public static Enrollment registered(RegistrationResponse registration) {
        return new Enrollment(registration, null);
    }

    public static Enrollment waiting(WaitingListResponse waiting) {
        return new Enrollment(null, waiting);
    }

    public boolean isWaiting() {
        return waiting != null;
    }
}
