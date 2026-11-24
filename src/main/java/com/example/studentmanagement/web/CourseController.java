package com.example.studentmanagement.web;

import com.example.studentmanagement.service.CourseService;
import com.example.studentmanagement.web.dto.CourseRequest;
import com.example.studentmanagement.web.dto.CourseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courses;

    public CourseController(CourseService courses) {
        this.courses = courses;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return courses.create(request);
    }

    @GetMapping
    public List<CourseResponse> findAll() {
        return courses.findAll();
    }

    @GetMapping("/{id}")
    public CourseResponse findById(@PathVariable Long id) {
        return courses.findById(id);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courses.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courses.delete(id);
    }
}
