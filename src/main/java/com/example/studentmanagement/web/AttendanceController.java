package com.example.studentmanagement.web;

import com.example.studentmanagement.service.AttendanceService;
import com.example.studentmanagement.web.dto.AttendanceRequest;
import com.example.studentmanagement.web.dto.AttendanceResponse;
import com.example.studentmanagement.web.dto.AttendanceStatusRequest;
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
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendances;

    public AttendanceController(AttendanceService attendances) {
        this.attendances = attendances;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse create(@Valid @RequestBody AttendanceRequest request) {
        return attendances.create(request);
    }

    @GetMapping
    public List<AttendanceResponse> findAll(@RequestParam(required = false) Long studentId) {
        return attendances.findAll(studentId);
    }

    @GetMapping("/{id}")
    public AttendanceResponse findById(@PathVariable Long id) {
        return attendances.findById(id);
    }

    @PutMapping("/{id}/status")
    public AttendanceResponse updateStatus(@PathVariable Long id, @Valid @RequestBody AttendanceStatusRequest request) {
        return attendances.updateStatus(id, request);
    }
}
