package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.persistence.AttendanceRepository;
import com.example.studentmanagement.persistence.CourseRepository;
import com.example.studentmanagement.persistence.ExamRepository;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.persistence.SessionRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.CourseRequest;
import com.example.studentmanagement.web.dto.CourseResponse;
import com.example.studentmanagement.web.dto.GradingWeightsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courses;
    private final RegistrationRepository registrations;
    private final ExamRepository exams;
    private final GradeRepository grades;
    private final AttendanceRepository attendances;
    private final SessionRepository sessions;

    public CourseService(CourseRepository courses,
                         RegistrationRepository registrations,
                         ExamRepository exams,
                         GradeRepository grades,
                         AttendanceRepository attendances,
                         SessionRepository sessions) {
        this.courses = courses;
        this.registrations = registrations;
        this.exams = exams;
        this.grades = grades;
        this.attendances = attendances;
        this.sessions = sessions;
    }

    public CourseResponse create(CourseRequest request) {
        rejectDuplicateCode(request.code(), null);
        Course saved = courses.save(new Course(request.code(), request.title(), request.capacity()));
        return CourseResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courses.findAll().stream().map(CourseResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return CourseResponse.of(load(id));
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course course = load(id);
        rejectDuplicateCode(request.code(), id);
        long taken = registrations.countByCourseIdAndStatus(id, RegistrationStatus.VALIDATED);
        if (request.capacity() < taken) {
            throw ApiException.badRequest(
                    "Capacity cannot be lower than the " + taken + " validated registrations");
        }
        course.revise(request.code(), request.title(), request.capacity());
        return CourseResponse.of(course);
    }

    public void delete(Long id) {
        Course course = load(id);
        for (Course owner : courses.findByRequiredCourses_Id(id)) {
            owner.dropRequirement(id);
        }
        for (Course owner : courses.findByEquivalentCourses_Id(id)) {
            owner.dropEquivalent(id);
        }
        attendances.deleteBySessionCourseId(id);
        sessions.deleteByCourseId(id);
        grades.deleteByExamCourseId(id);
        exams.deleteByCourseId(id);
        registrations.deleteByCourseId(id);
        courses.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> prerequisites(Long courseId) {
        return load(courseId).requiredCourses().stream().map(CourseResponse::of).toList();
    }

    public CourseResponse addPrerequisite(Long courseId, Long requiredCourseId) {
        Course course = load(courseId);
        Course required = load(requiredCourseId);
        if (courseId.equals(requiredCourseId)) {
            throw ApiException.badRequest("A course cannot be its own prerequisite");
        }
        if (course.alreadyRequires(requiredCourseId)) {
            throw ApiException.conflict(required.getCode() + " is already a prerequisite of " + course.getCode());
        }
        course.require(required);
        return CourseResponse.of(course);
    }

    public CourseResponse removePrerequisite(Long courseId, Long requiredCourseId) {
        Course course = load(courseId);
        Course required = load(requiredCourseId);
        if (!course.dropRequirement(requiredCourseId)) {
            throw ApiException.badRequest(required.getCode() + " is not a prerequisite of " + course.getCode());
        }
        return CourseResponse.of(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> equivalents(Long courseId) {
        return load(courseId).equivalentCourses().stream().map(CourseResponse::of).toList();
    }

    public CourseResponse addEquivalent(Long courseId, Long equivalentCourseId) {
        Course course = load(courseId);
        Course equivalent = load(equivalentCourseId);
        if (courseId.equals(equivalentCourseId)) {
            throw ApiException.badRequest("A course cannot be equivalent to itself");
        }
        if (course.alreadyAccepts(equivalentCourseId)) {
            throw ApiException.conflict(equivalent.getCode() + " is already an equivalent of " + course.getCode());
        }
        course.acceptEquivalent(equivalent);
        return CourseResponse.of(course);
    }

    public CourseResponse removeEquivalent(Long courseId, Long equivalentCourseId) {
        Course course = load(courseId);
        Course equivalent = load(equivalentCourseId);
        if (!course.dropEquivalent(equivalentCourseId)) {
            throw ApiException.badRequest(equivalent.getCode() + " is not an equivalent of " + course.getCode());
        }
        return CourseResponse.of(course);
    }

    public CourseResponse updateWeights(Long id, GradingWeightsRequest request) {
        int total = request.midtermWeight() + request.finalWeight() + request.projectWeight();
        if (total != 100) {
            throw ApiException.badRequest("Exam weights must total 100");
        }
        Course course = load(id);
        course.replaceWeights(request.midtermWeight(), request.finalWeight(), request.projectWeight());
        return CourseResponse.of(course);
    }

    public Course load(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> ApiException.notFound("Course " + id + " was not found"));
    }

    private void rejectDuplicateCode(String code, Long currentId) {
        boolean taken = currentId == null
                ? courses.existsByCodeIgnoreCase(code)
                : courses.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (taken) {
            throw ApiException.conflict("Course code already used: " + code);
        }
    }
}
