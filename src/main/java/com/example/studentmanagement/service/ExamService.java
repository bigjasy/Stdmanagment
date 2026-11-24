package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.exam.ExamCatalog;
import com.example.studentmanagement.exam.ExamMirror;
import com.example.studentmanagement.exam.RemoteExam;
import com.example.studentmanagement.exam.RemoteExamDraft;
import com.example.studentmanagement.persistence.ExamRepository;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.ExamRequest;
import com.example.studentmanagement.web.dto.ExamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExamService {

    private static final Logger LOG = LoggerFactory.getLogger(ExamService.class);

    private final ExamRepository exams;
    private final GradeRepository grades;
    private final CourseService courses;
    private final ExamCatalog catalog;
    private final ExamMirror mirror;

    public ExamService(ExamRepository exams, GradeRepository grades, CourseService courses,
                       ExamCatalog catalog, ExamMirror mirror) {
        this.exams = exams;
        this.grades = grades;
        this.courses = courses;
        this.catalog = catalog;
        this.mirror = mirror;
    }

    public ExamResponse create(ExamRequest request) {
        Course course = findCourse(request.courseId());
        RemoteExam created = catalog.create(new RemoteExamDraft(
                course.getCode(), request.date().toString(), request.type().name()));
        return ExamResponse.of(upsert(created));
    }

    public List<ExamResponse> findAll(Long courseId) {
        try {
            List<RemoteExam> remote = courseId == null
                    ? catalog.list()
                    : catalog.listByCourse(findCourse(courseId).getCode());
            return remote.stream().map(this::upsert).map(ExamResponse::of).toList();
        } catch (ApiException exception) {
            if (exception.status() != 503) {
                throw exception;
            }
            LOG.warn("External exam service unavailable; serving the local mirror");
            List<Exam> local = courseId == null ? exams.findAll() : exams.findByCourseId(courseId);
            return local.stream().map(ExamResponse::of).toList();
        }
    }

    public ExamResponse findById(Long id) {
        return ExamResponse.of(load(id));
    }

    public Exam load(Long id) {
        Exam local = exams.findById(id)
                .orElseThrow(() -> ApiException.notFound("Exam " + id + " was not found"));
        if (local.getExternalId() == null) {
            return local;
        }
        try {
            Optional<RemoteExam> remote = catalog.find(local.getExternalId());
            if (remote.isEmpty()) {
                local.cancel();
                return exams.save(local);
            }
            return upsert(remote.get());
        } catch (ApiException exception) {
            if (exception.status() != 503) {
                throw exception;
            }
            LOG.warn("External exam service unavailable; returning local exam {}", id);
            return local;
        }
    }

    private Exam upsert(RemoteExam remote) {
        boolean graded = exams.findByExternalId(remote.externalId())
                .map(existing -> grades.existsByExamId(existing.getId()))
                .orElse(false);
        return exams.findByExternalId(remote.externalId())
                .map(existing -> exams.save(mirror.apply(existing, remote, graded)))
                .orElseGet(() -> exams.save(mirror.create(remote)));
    }

    private Course findCourse(Long id) {
        try {
            return courses.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Course not found with id " + id);
        }
    }
}
