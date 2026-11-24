package com.example.studentmanagement.service;

import com.example.studentmanagement.domain.Course;
import com.example.studentmanagement.domain.RegistrationStatus;
import com.example.studentmanagement.persistence.AttendanceRepository;
import com.example.studentmanagement.persistence.CourseRepository;
import com.example.studentmanagement.persistence.ExamRepository;
import com.example.studentmanagement.persistence.GradeRepository;
import com.example.studentmanagement.persistence.RegistrationRepository;
import com.example.studentmanagement.persistence.SessionRepository;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.support.TestIds;
import com.example.studentmanagement.web.dto.CourseRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courses;
    @Mock
    private RegistrationRepository registrations;
    @Mock
    private ExamRepository exams;
    @Mock
    private GradeRepository grades;
    @Mock
    private AttendanceRepository attendances;
    @Mock
    private SessionRepository sessions;
    @InjectMocks
    private CourseService service;

    @Test
    void createStoresTheCodeInUpperCase() {
        when(courses.existsByCodeIgnoreCase("cs101")).thenReturn(false);
        when(courses.save(any(Course.class))).thenAnswer(invocation -> TestIds.withId(invocation.getArgument(0), 1L));

        assertThat(service.create(new CourseRequest("cs101", "Algorithms", 30)).code()).isEqualTo("CS101");
    }

    @Test
    void updateRejectsACapacityBelowCurrentRegistrations() {
        Course course = TestIds.withId(new Course("CS101", "Algorithms", 2), 1L);
        when(courses.findById(1L)).thenReturn(Optional.of(course));
        when(courses.existsByCodeIgnoreCaseAndIdNot("CS101", 1L)).thenReturn(false);
        when(registrations.countByCourseIdAndStatus(1L, RegistrationStatus.VALIDATED)).thenReturn(2L);

        assertThatThrownBy(() -> service.update(1L, new CourseRequest("CS101", "Algorithms", 1)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Capacity cannot be lower");
    }

    @Test
    void deleteRemovesDependentsFirst() {
        when(courses.findById(1L)).thenReturn(Optional.of(TestIds.withId(new Course("CS101", "Algorithms", 2), 1L)));
        when(courses.findByRequiredCourses_Id(1L)).thenReturn(List.of());
        when(courses.findByEquivalentCourses_Id(1L)).thenReturn(List.of());

        service.delete(1L);

        verify(grades).deleteByExamCourseId(1L);
        verify(exams).deleteByCourseId(1L);
        verify(registrations).deleteByCourseId(1L);
        verify(courses).deleteById(1L);
    }

    @Test
    void addPrerequisiteRejectsASelfLink() {
        when(courses.findById(1L)).thenReturn(Optional.of(TestIds.withId(new Course("CS101", "Algorithms", 2), 1L)));

        assertThatThrownBy(() -> service.addPrerequisite(1L, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessage("A course cannot be its own prerequisite");
    }

    @Test
    void deleteUnlinksCoursesThatDependOnTheDeletedCourse() {
        Course target = TestIds.withId(new Course("CS100", "Intro", 20), 1L);
        Course owner = TestIds.withId(new Course("CS101", "Algorithms", 20), 2L);
        owner.require(target);
        when(courses.findById(1L)).thenReturn(Optional.of(target));
        when(courses.findByRequiredCourses_Id(1L)).thenReturn(List.of(owner));
        when(courses.findByEquivalentCourses_Id(1L)).thenReturn(List.of());

        service.delete(1L);

        assertThat(owner.requiredCourses()).isEmpty();
    }

    @Test
    void updateWeightsRejectsATotalOtherThanOneHundred() {
        assertThatThrownBy(() -> service.updateWeights(1L, new com.example.studentmanagement.web.dto.GradingWeightsRequest(40, 40, 10)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Exam weights must total 100");
    }
}
