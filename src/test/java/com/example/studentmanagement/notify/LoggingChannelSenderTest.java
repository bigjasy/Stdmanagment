package com.example.studentmanagement.notify;

import com.example.studentmanagement.domain.NotificationChannel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoggingChannelSenderTest {

    @Test
    void aMissingPhoneNumberFailsTheSmsChannel() {
        ChannelSender sender = new LoggingChannelSender(NotificationChannel.SMS) {
        };

        assertThatThrownBy(() -> sender.send("  ", "hello"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SMS destination is missing");
    }
}
