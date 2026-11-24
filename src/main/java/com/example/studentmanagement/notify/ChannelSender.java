package com.example.studentmanagement.notify;

import com.example.studentmanagement.domain.NotificationChannel;

public interface ChannelSender {

    NotificationChannel channel();

    void send(String recipient, String message);
}
