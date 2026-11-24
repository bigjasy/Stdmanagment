package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.ExamType;
import com.example.studentmanagement.domain.Grade;
import com.example.studentmanagement.domain.GradeStatus;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.web.dto.GradeRequest;
import com.example.studentmanagement.web.dto.GradeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GradeService {

    private final GradeRepository grades;
    private final ExamService exams;
    private final StudentService students;
    private final AbsenceRuleService absenceRules;

    public GradeService(GradeRepository grades, ExamService exams, StudentService students,
                        AbsenceRuleService absenceRules) {
        this.grades = grades;
        this.exams = exams;
        this.students = students;
        this.absenceRules = absenceRules;
    }

    public GradeResponse create(GradeRequest request) {
        Exam exam = findExam(request.examId());
        Student student = findStudent(request.studentId());
        if (grades.existsByExamIdAndStudentId(exam.getId(), student.getId())) {
            throw ApiException.conflict("Student " + student.getId()
                    + " already has a grade for exam " + exam.getId());
        }
        Grade grade = new Grade(exam, student, request.value());
        absenceRules.courseBlock(student.getId(), exam.getCourse())
                .or(() -> exam.getType() == ExamType.FINAL
                        ? absenceRules.semesterBlock(student.getId())
                        : java.util.Optional.empty())
                .ifPresent(grade::holdForAdministration);
        Grade saved = grades.save(grade);
        return GradeResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> findByStudent(Long studentId) {
        students.load(studentId);
        return grades.findByStudentId(studentId).stream().map(GradeResponse::of).toList();
    }

    private Exam findExam(Long id) {
        try {
            return exams.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Exam not found with id " + id);
        }
    }

    private Student findStudent(Long id) {
        try {
            return students.load(id);
        } catch (ApiException exception) {
            throw ApiException.badRequest("Student not found with id " + id);
        }
    }
}
