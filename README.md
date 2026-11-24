# studentmanagement (Spring Boot + MySQL / XAMPP)

Independent implementation of the student-management API. The rules match the exercise, and the code is organized as request records, response records, services, and pure checks. JPA entities are not returned from the API.

Branches:

- `main` starts as the base API below. Each exercise is added on the next branch and then merged forward.
- `change-1` through `change-6` are the exercises. Open the branch README at the **Changes N** section to see which classes moved and which behavior changed.

## Prerequisites

- JDK 17
- Maven 3.9+
- XAMPP with MySQL started (Apache is not required)

## Database

The database `studentmanagement` is created automatically. Hibernate updates the tables (`ddl-auto=update`).

Default XAMPP credentials are in `src/main/resources/application.properties`: user `root`, empty password, port **3306**.

If an older schema is already in that database, drop it before the first run:

    SET FOREIGN_KEY_CHECKS = 0;
    DROP TABLE IF EXISTS grades, exams, registrations, students, users, courses;
    SET FOREIGN_KEY_CHECKS = 1;

## Run

    mvn spring-boot:run

The API listens on http://localhost:8080.

## Tests

    mvn test

The suite does not need MySQL. This base has **15 tests** covering student, course, registration, and grade rules, plus the student HTTP contract.

## Git hooks

A `pre-push` hook lives at `.githooks/pre-push`. Enable it once after cloning:

    git config core.hooksPath .githooks

Every `git push` then runs `mvn test` and cancels the push when a test fails. Java 17 is required.

## Model

- User (id, name, role) is the parent of Student (email, program). Joined inheritance uses the tables `users` and `students`.
- Course (code, title, capacity).
- Registration (student, course, status). A student can register for a course only once.
- Exam (course, date).
- Grade (exam, student, value from 0 to 20). One grade per student and exam.

## Endpoints

| Method | Endpoint | Result |
|---|---|---|
| POST | /students | 201 |
| GET | /students, /students/{id} | 200 / 404 |
| PUT | /students/{id} | 200 / 404 / 409 |
| DELETE | /students/{id} | 204 / 404 |
| GET | /students/{id}/registrations | 200 / 404 |
| GET | /students/{id}/grades | 200 / 404 |
| POST | /courses | 201 / 409 |
| GET | /courses, /courses/{id} | 200 / 404 |
| PUT | /courses/{id} | 200 / 400 / 404 / 409 |
| DELETE | /courses/{id} | 204 / 404 |
| POST | /registrations | 201 / 400 (unknown student, unknown course, or course full) / 409 |
| POST | /exams | 201 / 400 |
| GET | /exams, /exams/{id} | 200 / 404 |
| GET | /exams?courseId={id} | 200 |
| POST | /grades | 201 / 400 / 409 |

## Quick test

    curl -X POST localhost:8080/students -H "Content-Type: application/json" -d "{\"name\":\"Mohamed Alaoui\",\"email\":\"m@example.com\",\"program\":\"Genie Informatique\"}"
    curl -X POST localhost:8080/courses -H "Content-Type: application/json" -d "{\"code\":\"CS101\",\"title\":\"Algorithms\",\"capacity\":30}"
    curl -X POST localhost:8080/registrations -H "Content-Type: application/json" -d "{\"studentId\":1,\"courseId\":1}"
    curl -X POST localhost:8080/exams -H "Content-Type: application/json" -d "{\"courseId\":1,\"date\":\"2026-12-20\"}"
    curl -X POST localhost:8080/grades -H "Content-Type: application/json" -d "{\"examId\":1,\"studentId\":1,\"value\":15.5}"
    curl localhost:8080/students/1/grades
