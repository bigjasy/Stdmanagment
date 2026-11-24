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
    DROP TABLE IF EXISTS grades, exams, registrations, course_equivalents, course_prerequisites, students, users, courses;
    SET FOREIGN_KEY_CHECKS = 1;

## Run

    mvn spring-boot:run

The API listens on http://localhost:8080.

## Tests

    mvn test

The suite does not need MySQL. This branch has **27 tests**.

## Git hooks

A `pre-push` hook lives at `.githooks/pre-push`. Enable it once after cloning:

    git config core.hooksPath .githooks

Every `git push` then runs `mvn test` and cancels the push when a test fails. Java 17 is required.

## Changes 1 — Course prerequisites on registration

Registration checks the previous academic year (1 September to 31 August) before it looks at capacity. A required course is satisfied only by a grade of at least 10 in that window. A lower grade is failed, and no grade is absent. A passed equivalent declared on the required course also satisfies the rule. Otherwise the API answers 400 and names every missing prerequisite, for example `Prerequisites not satisfied for course CS101: CS100 (failed)`.

### How many classes were changed

**15 Java classes** — 11 production and 4 test classes, plus this README:

- Production added: `AcademicCalendar`, `PrerequisitePolicy`, `TimeConfig`, `PrerequisiteRequest`, `EquivalentCourseRequest`.
- Production updated: `Course`, `CourseRepository`, `GradeRepository`, `CourseService`, `RegistrationService`, `CourseController`.
- Tests added: `AcademicCalendarTest`, `PrerequisitePolicyTest`.
- Tests updated: `CourseServiceTest`, `RegistrationServiceTest`.

### Functionalities affected

- Registration creation validates prerequisites after the student and course are loaded, and before the duplicate and capacity checks.
- Course management gained six endpoints to list, add, and remove prerequisites and equivalents.
- Deleting a course clears incoming prerequisite and equivalent links first, so the new foreign keys do not block the delete.
- Grade lookup can now filter by student, course, and exam date. Only the previous academic year is used.

### Regression tests needed

**8 new tests** (15 → **23 total**): academic-year boundaries, pass/fail/absent judgement, a failed prerequisite, a passed equivalent, a self-link rejection, and delete-time unlinking. `mvn test` does not need MySQL.

### Development and validation time

About 1 hour 30 minutes: design and implementation, then a full `mvn test` run on JDK 17.

### Unrelated functionality that broke

Deleting a course failed as soon as another course referenced it as a prerequisite or an equivalent. `CourseService.delete` now removes those links before the course row, and a regression test covers it.

## Changes 2 — Weighted course grade

A course grade is computed from exam grades. Each exam is `MIDTERM`, `FINAL`, or `PROJECT` (omitted type defaults to `MIDTERM`). Grades in one category are averaged, then the course weights are applied. The standard scheme is 30 / 50 / 20 and can be replaced per course when the three weights total 100. A category with no grade is ignored, and the result is divided by the weights that were actually used. Results are rounded to two decimals.

### How many classes were changed

**16 Java classes** — 14 production and 2 test classes, plus this README:

- Production added: `ExamType`, `WeightedGradeCalculator`, `GradingWeightsRequest`, `CourseGradeResponse`, `CourseGradeService`.
- Production updated: `Exam`, `ExamRequest`, `ExamResponse`, `Course`, `CourseResponse`, `ExamService`, `CourseService`, `CourseController`, `GradeRepository`.
- Tests added: `WeightedGradeCalculatorTest`.
- Tests updated: `CourseServiceTest`.

### Functionalities affected

- `POST /exams` accepts an optional `type`.
- `PUT /courses/{id}/weights` replaces the 30/50/20 scheme. Any other total answers 400.
- Course JSON now includes the three weights.
- `GET /courses/{id}/grades/{studentId}` returns the averages, the weights, the applied weight, and the final grade.

### Regression tests needed

**4 new tests** (23 → **27 total**): the standard 30/50/20 combination, a missing category, legacy null weights, and a rejected weight total. `mvn test` does not need MySQL.

### Development and validation time

About 1 hour 20 minutes, including a full `mvn test` run on JDK 17.

### Unrelated functionality that broke

Adding the new exam type and weight columns as `NOT NULL` would stop Hibernate from updating a database that already has rows, which would take down every endpoint. The columns stay nullable, and `Exam.getType()` plus the course weight methods fall back to midterm and 30/50/20 when a legacy value is null.

## Model

- User (id, name, role) is the parent of Student (email, program). Joined inheritance uses the tables `users` and `students`.
- Course (code, title, capacity, required courses, equivalent courses, midterm weight 30, final weight 50, project weight 20).
- Registration (student, course, status). A student can register for a course only once, and only when every prerequisite was passed in the previous academic year.
- Exam (course, date, type `MIDTERM`, `FINAL`, or `PROJECT`).
- Grade (exam, student, value from 0 to 20). One grade per student and exam. The course result is calculated from those grades.

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
| GET | /courses/{id}/prerequisites | 200 / 404 |
| POST | /courses/{id}/prerequisites | 201 / 400 / 404 / 409 |
| DELETE | /courses/{id}/prerequisites/{requiredCourseId} | 200 / 400 / 404 |
| GET | /courses/{id}/equivalents | 200 / 404 |
| POST | /courses/{id}/equivalents | 201 / 400 / 404 / 409 |
| DELETE | /courses/{id}/equivalents/{equivalentCourseId} | 200 / 400 / 404 |
| PUT | /courses/{id}/weights | 200 / 400 / 404 |
| GET | /courses/{id}/grades/{studentId} | 200 / 404 |
| POST | /registrations | 201 / 400 (unknown student, unknown course, prerequisites, or course full) / 409 |
| POST | /exams | 201 / 400 (optional type: MIDTERM, FINAL, PROJECT) |
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
