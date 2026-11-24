package com.example.studentmanagement.web;

import com.example.studentmanagement.service.ExamService;
import com.example.studentmanagement.web.dto.ExamRequest;
import com.example.studentmanagement.web.dto.ExamResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/exams")
public class ExamController {

    private final ExamService exams;

    public ExamController(ExamService exams) {
        this.exams = exams;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamResponse create(@Valid @RequestBody ExamRequest request) {
        return exams.create(request);
    }

    @GetMapping
    public List<ExamResponse> findAll(@RequestParam(required = false) Long courseId) {
        return exams.findAll(courseId);
    }

    @GetMapping("/{id}")
    public ExamResponse findById(@PathVariable Long id) {
        return exams.findById(id);
    }
}
