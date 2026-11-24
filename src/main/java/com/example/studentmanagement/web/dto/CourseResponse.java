package com.example.studentmanagement.web.dto;

import com.example.studentmanagement.domain.Course;

public record CourseResponse(Long id, String code, String title, Integer capacity) {

    public static CourseResponse of(Course course) {
        return new CourseResponse(course.getId(), course.getCode(), course.getTitle(), course.getCapacity());
    }
}
