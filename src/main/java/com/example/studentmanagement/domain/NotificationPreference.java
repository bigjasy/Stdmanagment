package com.example.studentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notification_preferences", uniqueConstraints = @UniqueConstraint(
        name = "uk_notification_preference_student", columnNames = "student_id"))
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled;

    @Column(name = "whatsapp_enabled", nullable = false)
    private boolean whatsappEnabled;

    protected NotificationPreference() {
    }

    public NotificationPreference(Student student) {
        this.student = student;
    }

    public void replace(boolean email, boolean sms, boolean whatsapp) {
        this.emailEnabled = email;
        this.smsEnabled = sms;
        this.whatsappEnabled = whatsapp;
    }

    public List<NotificationChannel> enabledChannels() {
        List<NotificationChannel> channels = new ArrayList<>();
        if (emailEnabled) {
            channels.add(NotificationChannel.EMAIL);
        }
        if (smsEnabled) {
            channels.add(NotificationChannel.SMS);
        }
        if (whatsappEnabled) {
            channels.add(NotificationChannel.WHATSAPP);
        }
        return channels;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public boolean isWhatsappEnabled() {
        return whatsappEnabled;
    }
}
