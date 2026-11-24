package com.example.studentmanagement.academic;

import com.example.studentmanagement.discovery.ServiceDirectory;
import com.example.studentmanagement.domain.Student;
import com.example.studentmanagement.shared.ApiException;
import com.example.studentmanagement.support.TestIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestTemplateAcademicClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private Student student;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplateBuilder().build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        student = TestIds.withId(new Student("Ada", "ada@school.test", "CS"), 7L);
    }

    @Test
    void callsTheNextInstanceOnTheFollowingLookup() {
        RestTemplateAcademicClient client = client("http://localhost:8082", "http://localhost:8083");
        server.expect(requestTo("http://localhost:8082/students/7/enrollment"))
                .andRespond(withSuccess(active(), MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8083/students/7/enrollment"))
                .andRespond(withSuccess(active(), MediaType.APPLICATION_JSON));

        client.requireActiveEnrollment(student);
        client.requireActiveEnrollment(student);

        server.verify();
    }

    @Test
    void failsOverWhenTheFirstInstanceIsDown() {
        RestTemplateAcademicClient client = client("http://localhost:8082", "http://localhost:8083");
        server.expect(requestTo("http://localhost:8082/students/7/enrollment"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo("http://localhost:8083/students/7/enrollment"))
                .andRespond(withSuccess(active(), MediaType.APPLICATION_JSON));

        client.requireActiveEnrollment(student);

        server.verify();
    }

    @Test
    void hidesThePhysicalAddressWhenEveryInstanceIsDown() {
        RestTemplateAcademicClient client = client("http://localhost:8082", "http://localhost:8083");
        server.expect(requestTo("http://localhost:8082/students/7/enrollment"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo("http://localhost:8083/students/7/enrollment"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.requireActiveEnrollment(student))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> {
                    ApiException exception = (ApiException) error;
                    assertThat(exception.status()).isEqualTo(503);
                    assertThat(exception.getMessage()).isEqualTo("Academic service is unavailable");
                    assertThat(exception.getMessage()).doesNotContain("localhost").doesNotContain("8082");
                });
    }

    @Test
    void treatsNotFoundAsAnAnswerFromAcademic() {
        RestTemplateAcademicClient client = client("http://localhost:8082", "http://localhost:8083");
        server.expect(requestTo("http://localhost:8082/students/7/enrollment"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.requireActiveEnrollment(student))
                .isInstanceOf(ApiException.class)
                .hasMessage("Student 7 is unknown to Academic");

        server.verify();
    }

    @Test
    void rejectsAStudentWhoIsNotActive() {
        RestTemplateAcademicClient client = client("http://localhost:8082");
        server.expect(requestTo("http://localhost:8082/students/7/enrollment"))
                .andRespond(withSuccess("{\"studentId\":7,\"active\":false,\"program\":\"CS\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.requireActiveEnrollment(student))
                .isInstanceOf(ApiException.class)
                .hasMessage("Student 7 is not active in Academic");
    }

    private RestTemplateAcademicClient client(String... instances) {
        return new RestTemplateAcademicClient(restTemplate, new ListedDirectory(instances));
    }

    private static String active() {
        return "{\"studentId\":7,\"active\":true,\"program\":\"CS\"}";
    }

    private static final class ListedDirectory implements ServiceDirectory {
        private final List<URI> instances;

        private ListedDirectory(String... urls) {
            this.instances = Arrays.stream(urls).map(URI::create).toList();
        }

        @Override
        public List<URI> instances(String serviceName) {
            return instances;
        }
    }
}
