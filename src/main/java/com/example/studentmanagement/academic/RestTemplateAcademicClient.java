package com.example.studentmanagement.academic;

import com.example.studentmanagement.discovery.ServiceDirectory;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.shared.ApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RestTemplateAcademicClient implements AcademicClient {

    static final String SERVICE = "academic";

    private final RestTemplate restTemplate;
    private final ServiceDirectory directory;
    private final AtomicInteger cursor = new AtomicInteger();

    public RestTemplateAcademicClient(RestTemplate academicRestTemplate, ServiceDirectory directory) {
        this.restTemplate = academicRestTemplate;
        this.directory = directory;
    }

    @Override
    public void requireActiveEnrollment(Student student) {
        List<URI> instances = directory.instances(SERVICE);
        if (instances.isEmpty()) {
            throw ApiException.unavailable("Academic service is unavailable");
        }
        int start = Math.floorMod(cursor.getAndIncrement(), instances.size());
        for (int offset = 0; offset < instances.size(); offset++) {
            URI base = instances.get((start + offset) % instances.size());
            try {
                AcademicEnrollment enrollment = restTemplate.getForObject(
                        base + "/students/{id}/enrollment",
                        AcademicEnrollment.class,
                        student.getId());
                accept(student, enrollment);
                return;
            } catch (HttpClientErrorException.NotFound ignored) {
                throw ApiException.badRequest("Student " + student.getId() + " is unknown to Academic");
            } catch (HttpStatusCodeException http) {
                if (http.getStatusCode().is5xxServerError()) {
                    continue;
                }
                throw ApiException.badGateway("Academic rejected the enrollment lookup");
            } catch (ResourceAccessException ignored) {
                // This instance did not answer. The next configured instance is tried.
            }
        }
        throw ApiException.unavailable("Academic service is unavailable");
    }

    private static void accept(Student student, AcademicEnrollment enrollment) {
        if (enrollment == null || !enrollment.active()) {
            throw ApiException.badRequest("Student " + student.getId() + " is not active in Academic");
        }
    }
}
