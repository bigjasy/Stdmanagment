package com.example.studentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {

    public static final int DEFAULT_MIDTERM_WEIGHT = 30;
    public static final int DEFAULT_FINAL_WEIGHT = 50;
    public static final int DEFAULT_PROJECT_WEIGHT = 20;

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

    @Column(name = "midterm_weight")
    private Integer midtermWeight;

    @Column(name = "final_weight")
    private Integer finalWeight;

    @Column(name = "project_weight")
    private Integer projectWeight;

    @ManyToMany
    @JoinTable(name = "course_prerequisites",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "required_course_id"))
    private Set<Course> requiredCourses = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "course_equivalents",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "equivalent_course_id"))
    private Set<Course> equivalentCourses = new LinkedHashSet<>();

    protected Course() {
    }

    public Course(String code, String title, int capacity) {
        this.code = code.trim().toUpperCase();
        this.title = title.trim();
        this.capacity = capacity;
        this.midtermWeight = DEFAULT_MIDTERM_WEIGHT;
        this.finalWeight = DEFAULT_FINAL_WEIGHT;
        this.projectWeight = DEFAULT_PROJECT_WEIGHT;
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

    public int midtermWeight() {
        return midtermWeight == null ? DEFAULT_MIDTERM_WEIGHT : midtermWeight;
    }

    public int finalWeight() {
        return finalWeight == null ? DEFAULT_FINAL_WEIGHT : finalWeight;
    }

    public int projectWeight() {
        return projectWeight == null ? DEFAULT_PROJECT_WEIGHT : projectWeight;
    }

    public void replaceWeights(int midterm, int finale, int project) {
        this.midtermWeight = midterm;
        this.finalWeight = finale;
        this.projectWeight = project;
    }

    public void require(Course required) {
        requiredCourses.add(required);
    }

    public boolean alreadyRequires(Long courseId) {
        return requiredCourses.stream().anyMatch(course -> courseId.equals(course.getId()));
    }

    public boolean dropRequirement(Long courseId) {
        return requiredCourses.removeIf(course -> courseId.equals(course.getId()));
    }

    public void acceptEquivalent(Course equivalent) {
        equivalentCourses.add(equivalent);
    }

    public boolean alreadyAccepts(Long courseId) {
        return equivalentCourses.stream().anyMatch(course -> courseId.equals(course.getId()));
    }

    public boolean dropEquivalent(Long courseId) {
        return equivalentCourses.removeIf(course -> courseId.equals(course.getId()));
    }

    public Set<Course> requiredCourses() {
        return Set.copyOf(requiredCourses);
    }

    public Set<Course> equivalentCourses() {
        return Set.copyOf(equivalentCourses);
    }
}
