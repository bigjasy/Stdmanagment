package com.example.studentmanagement.web;

import com.example.studentmanagement.service.SessionService;
import com.example.studentmanagement.web.dto.SessionRequest;
import com.example.studentmanagement.web.dto.SessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessions;

    public SessionController(SessionService sessions) {
        this.sessions = sessions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse create(@Valid @RequestBody SessionRequest request) {
        return sessions.create(request);
    }

    @GetMapping
    public List<SessionResponse> findAll(@RequestParam(required = false) Long courseId) {
        return sessions.findAll(courseId);
    }

    @PutMapping("/{id}")
    public SessionResponse update(@PathVariable Long id, @Valid @RequestBody SessionRequest request) {
        return sessions.update(id, request);
    }

    @GetMapping("/{id}")
    public SessionResponse findById(@PathVariable Long id) {
        return sessions.findById(id);
    }
}
