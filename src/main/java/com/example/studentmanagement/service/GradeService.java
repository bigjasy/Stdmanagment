package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Exam;
import com.example.studentmanagement.domain.Grade;
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

    public GradeService(GradeRepository grades, ExamService exams, StudentService students) {
        this.grades = grades;
        this.exams = exams;
        this.students = students;
    }

    public GradeResponse create(GradeRequest request) {
        Exam exam = findExam(request.examId());
        Student student = findStudent(request.studentId());
        if (grades.existsByExamIdAndStudentId(exam.getId(), student.getId())) {
            throw ApiException.conflict("Student " + student.getId()
                    + " already has a grade for exam " + exam.getId());
        }
        Grade saved = grades.save(new Grade(exam, student, request.value()));
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
