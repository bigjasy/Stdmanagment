package com.example.studentmanagement.web;

import com.example.studentmanagement.service.GradeService;
import com.example.studentmanagement.web.dto.GradeRequest;
import com.example.studentmanagement.web.dto.GradeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grades")
public class GradeController {

    private final GradeService grades;

    public GradeController(GradeService grades) {
        this.grades = grades;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GradeResponse create(@Valid @RequestBody GradeRequest request) {
        return grades.create(request);
    }
}
