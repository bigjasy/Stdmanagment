package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.Grade;
import com.example.studentmanagement.domain.GradeStatus;
import com.example.studentmanagement.domain.Notification;
import com.example.studentmanagement.domain.NotificationChannel;
import com.example.studentmanagement.domain.NotificationPreference;
import com.example.studentmanagement.domain.NotificationType;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.domain.Session;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.notify.ChannelSender;
import com.example.studentmanagement.persistence.NotificationPreferenceRepository;
import com.example.studentmanagement.persistence.NotificationRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.web.dto.NotificationPreferenceRequest;
import com.example.studentmanagement.web.dto.NotificationPreferenceResponse;
import com.example.studentmanagement.web.dto.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final NotificationPreferenceRepository preferences;
    private final RegistrationRepository registrations;
    private final StudentService students;
    private final Map<NotificationChannel, ChannelSender> senders;

    public NotificationService(NotificationRepository notifications,
                               NotificationPreferenceRepository preferences,
                               RegistrationRepository registrations,
                               StudentService students,
                               List<ChannelSender> senderList) {
        this.notifications = notifications;
        this.preferences = preferences;
        this.registrations = registrations;
        this.students = students;
        this.senders = new EnumMap<>(NotificationChannel.class);
        senderList.forEach(sender -> this.senders.put(sender.channel(), sender));
    }

    public void registrationAccepted(Student student, Course course, Long registrationId) {
        send(student, NotificationType.REGISTRATION_ACCEPTED,
                "Registration accepted for " + course.getCode() + " - " + course.getTitle() + ".",
                registrationId);
    }

    public void timetableChanged(Session session) {
        String label = session.getLabel() == null ? "" : " (" + session.getLabel() + ")";
        String message = "Timetable changed for " + session.getCourse().getCode()
                + ": " + session.getDate() + label + ".";
        for (var registration : registrations.findByCourseIdAndStatus(
                session.getCourse().getId(), RegistrationStatus.VALIDATED)) {
            send(registration.getStudent(), NotificationType.TIMETABLE_CHANGED, message, session.getId());
        }
    }

    public void gradePosted(Student student, Exam exam, Grade grade) {
        String pending = grade.status() == GradeStatus.PENDING_ADMIN_DECISION
                ? " (pending administration decision)" : "";
        send(student, NotificationType.GRADE_POSTED,
                "Grade posted for " + exam.getCourse().getCode() + " - " + exam.getType()
                        + ": " + grade.getValue() + pending + ".",
                grade.getId());
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse preference(Long studentId) {
        Student student = students.load(studentId);
        return NotificationPreferenceResponse.of(preferences.findByStudentId(studentId)
                .orElseGet(() -> new NotificationPreference(student)));
    }

    public NotificationPreferenceResponse replacePreference(Long studentId, NotificationPreferenceRequest request) {
        Student student = students.load(studentId);
        NotificationPreference preference = preferences.findByStudentId(studentId)
                .orElseGet(() -> preferences.save(new NotificationPreference(student)));
        preference.replace(request.emailEnabled(), request.smsEnabled(), request.whatsappEnabled());
        return NotificationPreferenceResponse.of(preference);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> history(Long studentId) {
        students.load(studentId);
        return notifications.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(NotificationResponse::of)
                .toList();
    }

    private void send(Student student, NotificationType type, String message, Long referenceId) {
        NotificationPreference preference = preferences.findByStudentId(student.getId())
                .orElseGet(() -> new NotificationPreference(student));
        for (NotificationChannel channel : preference.enabledChannels()) {
            String recipient = channel == NotificationChannel.EMAIL ? student.getEmail() : student.getPhoneNumber();
            Notification notification = new Notification(student, type, channel, message, recipient, referenceId);
            try {
                ChannelSender sender = senders.get(channel);
                if (sender == null) {
                    throw new IllegalStateException("No sender configured for " + channel);
                }
                sender.send(recipient, message);
                notification.markSent();
            } catch (RuntimeException exception) {
                LOG.warn("Notification failed for student {} on {}: {}", student.getId(), channel, exception.getMessage());
                notification.markFailed(exception.getMessage());
            }
            notifications.save(notification);
        }
    }
}
