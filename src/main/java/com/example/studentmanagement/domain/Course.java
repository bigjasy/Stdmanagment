package com.example.studentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer capacity;

    protected Course() {
    }

    public Course(String code, String title, int capacity) {
        this.code = code.trim().toUpperCase();
        this.title = title.trim();
        this.capacity = capacity;
    }

    public void revise(String code, String title, int capacity) {
        this.code = code.trim().toUpperCase();
        this.title = title.trim();
        this.capacity = capacity;
    }

    public boolean hasSeatFor(long validatedRegistrations) {
        return validatedRegistrations < capacity;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
