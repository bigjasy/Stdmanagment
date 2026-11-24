package com.example.studentmanagement.exam;

import com.example.studentmanagement.shared.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Component
public class RestExamCatalog implements ExamCatalog {

    private static final Logger LOG = LoggerFactory.getLogger(RestExamCatalog.class);

    private final RestClient client;

    public RestExamCatalog(RestClient externalExamClient) {
        this.client = externalExamClient;
    }

    @Override
    public RemoteExam create(RemoteExamDraft draft) {
        try {
            RemoteExam created = client.post().uri("/exams").contentType(MediaType.APPLICATION_JSON)
                    .body(draft).retrieve().body(RemoteExam.class);
            if (created == null || created.externalId() == null || created.externalId().isBlank()) {
                throw ApiException.badGateway("External exam service returned no exam identifier");
            }
            return created;
        } catch (RestClientException exception) {
            throw translate(exception, "create an exam");
        }
    }

    @Override
    public List<RemoteExam> list() {
        return readList("/exams", null, "list exams");
    }

    @Override
    public List<RemoteExam> listByCourse(String courseCode) {
        return readList("/exams", courseCode, "list exams for course " + courseCode);
    }

    @Override
    public Optional<RemoteExam> find(String externalId) {
        try {
            return Optional.ofNullable(client.get().uri("/exams/{id}", externalId).retrieve().body(RemoteExam.class));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw translate(exception, "read exam " + externalId);
        }
    }

    private List<RemoteExam> readList(String path, String courseCode, String operation) {
        try {
            RemoteExam[] body = courseCode == null
                    ? client.get().uri(path).retrieve().body(RemoteExam[].class)
                    : client.get().uri(uri -> uri.path(path).queryParam("courseCode", courseCode).build())
                    .retrieve().body(RemoteExam[].class);
            return body == null ? List.of() : List.of(body);
        } catch (RestClientException exception) {
            throw translate(exception, operation);
        }
    }

    private RuntimeException translate(RestClientException exception, String operation) {
        LOG.warn("External exam service failed while trying to {}: {}", operation, exception.getMessage());
        if (exception instanceof HttpServerErrorException || exception instanceof ResourceAccessException) {
            return ApiException.unavailable("External exam service unavailable while trying to " + operation);
        }
        return ApiException.badGateway("External exam service rejected the attempt to " + operation);
    }
}
