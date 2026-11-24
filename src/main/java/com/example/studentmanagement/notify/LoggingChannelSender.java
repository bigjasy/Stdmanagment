package com.example.studentmanagement.notify;

import com.example.studentmanagement.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class LoggingChannelSender implements ChannelSender {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingChannelSender.class);

    private final NotificationChannel channel;

    public LoggingChannelSender(NotificationChannel channel) {
        this.channel = channel;
    }

    @Override
    public NotificationChannel channel() {
        return channel;
    }

    @Override
    public final void send(String recipient, String message) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException(channel + " destination is missing");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(channel + " message is empty");
        }
        LOG.info("[{}] to {}: {}", channel, recipient, message);
    }

    @Component
    static class Email extends LoggingChannelSender {
        Email() { super(NotificationChannel.EMAIL); }
    }

    @Component
    static class Sms extends LoggingChannelSender {
        Sms() { super(NotificationChannel.SMS); }
    }

    @Component
    static class WhatsApp extends LoggingChannelSender {
        WhatsApp() { super(NotificationChannel.WHATSAPP); }
    }
}
