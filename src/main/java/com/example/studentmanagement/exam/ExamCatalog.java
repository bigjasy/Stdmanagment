package com.example.studentmanagement.exam;

import java.util.List;
import java.util.Optional;

public interface ExamCatalog {

    RemoteExam create(RemoteExamDraft draft);

    List<RemoteExam> list();

    List<RemoteExam> listByCourse(String courseCode);

    Optional<RemoteExam> find(String externalId);
}
