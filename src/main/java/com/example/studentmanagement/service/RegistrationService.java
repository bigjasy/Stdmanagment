package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.AcademicCalendar;
import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.PrerequisitePolicy;
import com.example.studentmanagement.domain.Registration;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.domain.WaitingListEntry;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.persistence.WaitingListRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.RegistrationRequest;
import com.example.studentmanagement.web.dto.RegistrationResponse;
import com.example.studentmanagement.web.dto.WaitingListResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrations;
    private final GradeRepository grades;
    private final WaitingListRepository waitingList;
    private final StudentService students;
    private final CourseService courses;
    private final Clock clock;

    public RegistrationService(RegistrationRepository registrations,
                               GradeRepository grades,
                               WaitingListRepository waitingList,
                               StudentService students,
                               CourseService courses,
                               Clock clock) {
        this.registrations = registrations;
        this.grades = grades;
        this.waitingList = waitingList;
        this.students = students;
        this.courses = courses;
        this.clock = clock;
    }

    public Enrollment create(RegistrationRequest request) {
        Student student = findStudent(request.studentId());
        Course course = findCourseForUpdate(request.courseId());
        verifyPrerequisites(course, student);
        rejectDuplicate(student, course);
        long taken = registrations.countByCourseIdAndStatus(course.getId(), RegistrationStatus.VALIDATED);
        if (!course.hasSeatFor(taken)) {
            WaitingListEntry saved = waitingList.save(
                    new WaitingListEntry(course, student, LocalDateTime.now(clock)));
            int position = waitingList.findByCourseIdOrderByIdAsc(course.getId()).size();
            return Enrollment.waiting(WaitingListResponse.of(saved, position));
        }
        Registration saved = registrations.save(Registration.validated(student, course));
        return Enrollment.registered(RegistrationResponse.of(saved));
    }

    public void cancel(Long registrationId) {
        Registration registration = registrations.findById(registrationId)
                .orElseThrow(() -> ApiException.notFound("Registration " + registrationId + " was not found"));
        Course course = findCourseForUpdate(registration.getCourse().getId());
        registrations.delete(registration);
        promoteOldestEligible(course);
    }

    @Transactional(readOnly = true)
    public List<WaitingListResponse> waitingList(Long courseId) {
        try {
            courses.load(courseId);
        } catch (ApiException exception) {
            throw ApiException.notFound("Course " + courseId + " was not found");
        }
        List<WaitingListEntry> entries = waitingList.findByCourseIdOrderByIdAsc(courseId);
        List<WaitingListResponse> responses = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            responses.add(WaitingListResponse.of(entries.get(index), index + 1));
        }
        return responses;
    }

    public void leaveWaitingList(Long entryId) {
        WaitingListEntry entry = waitingList.findById(entryId)
                .orElseThrow(() -> ApiException.notFound("Waiting list entry " + entryId + " was not found"));
        findCourseForUpdate(entry.getCourse().getId());
        waitingList.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByStudent(Long studentId) {
        students.load(studentId);
        return registrations.findByStudentId(studentId).stream().map(RegistrationResponse::of).toList();
    }

    private void verifyPrerequisites(Course course, Student student) {
        List<String> missing = PrerequisitePolicy.unsatisfied(course, history(student));
        if (!missing.isEmpty()) {
            throw ApiException.badRequest("Prerequisites not satisfied for course "
                    + course.getCode() + ": " + String.join(", ", missing));
        }
    }

    private Student findStudent(Long id) {
        try {
            return students.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Student not found with id " + id);
        }
    }

    private Course findCourseForUpdate(Long id) {
        try {
            return courses.loadForUpdate(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Course not found with id " + id);
        }
    }

    private void promoteOldestEligible(Course course) {
        long taken = registrations.countByCourseIdAndStatus(course.getId(), RegistrationStatus.VALIDATED);
        if (!course.hasSeatFor(taken)) {
            return;
        }
        for (WaitingListEntry entry : waitingList.findByCourseIdOrderByIdAsc(course.getId())) {
            Student student = entry.getStudent();
            if (registrations.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
                continue;
            }
            if (!PrerequisitePolicy.unsatisfied(course, history(student)).isEmpty()) {
                continue;
            }
            registrations.save(Registration.validated(student, course));
            waitingList.delete(entry);
            return;
        }
    }

    private PrerequisitePolicy.GradeHistory history(Student student) {
        AcademicCalendar.DateRange period = AcademicCalendar.previousYear(LocalDate.now(clock));
        return courseId -> grades.findByStudentIdAndExamCourseIdAndExamDateBetween(
                student.getId(), courseId, period.from(), period.to());
    }

    private void rejectDuplicate(Student student, Course course) {
        if (registrations.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw ApiException.conflict("Student " + student.getId()
                    + " is already registered to course " + course.getCode());
        }
        if (waitingList.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw ApiException.conflict("Student " + student.getId()
                    + " is already on the waiting list for course " + course.getCode());
        }
    }
}
