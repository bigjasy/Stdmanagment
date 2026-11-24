package com.example.studentmanagement.web;

import com.example.studentmanagement.service.Enrollment;
import com.example.studentmanagement.service.RegistrationService;
import com.example.studentmanagement.web.dto.RegistrationRequest;
import com.example.studentmanagement.web.dto.WaitingListResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/registrations")
public class RegistrationController {

    private final RegistrationService registrations;

    public RegistrationController(RegistrationService registrations) {
        this.registrations = registrations;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RegistrationRequest request) {
        Enrollment enrollment = registrations.create(request);
        if (enrollment.isWaiting()) {
            return ResponseEntity.accepted().body(enrollment.waiting());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment.registration());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        registrations.cancel(id);
    }

    @GetMapping("/waiting-list")
    public List<WaitingListResponse> waitingList(@RequestParam Long courseId) {
        return registrations.waitingList(courseId);
    }

    @DeleteMapping("/waiting-list/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveWaitingList(@PathVariable Long id) {
        registrations.leaveWaitingList(id);
    }
}
