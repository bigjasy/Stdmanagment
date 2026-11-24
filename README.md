# studentmanagement (Spring Boot + MySQL / XAMPP)

Independent implementation of the student-management API. The rules match the exercise, and the code is organized as request records, response records, services, and pure checks. JPA entities are not returned from the API.

Branches:

- `main` starts as the base API below. Each exercise is added on the next branch and then merged forward.
- `change-1` through `change-7` are the exercises. Open the branch README at the **Changes N** section to see which classes moved and which behavior changed.

## Prerequisites

- JDK 17
- Maven 3.9+
- XAMPP with MySQL started (Apache is not required)

## Database

The database `studentmanagement` is created automatically. Hibernate updates the tables (`ddl-auto=update`).

Default XAMPP credentials are in `src/main/resources/application.properties`: user `root`, empty password, port **3306**.

If an older schema is already in that database, drop it before the first run:

    SET FOREIGN_KEY_CHECKS = 0;
    DROP TABLE IF EXISTS notifications, notification_preferences, attendances, sessions, grades, exams, waiting_list, registrations, course_equivalents, course_prerequisites, students, users, courses;
    SET FOREIGN_KEY_CHECKS = 1;

## Run

    mvn spring-boot:run

The API listens on http://localhost:8080.

## Tests

    mvn test

The suite does not need MySQL. This branch has **43 tests**.

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

## Changes 3 — Attendance, absence blocking and administration decision

Attendance is recorded per class session. A session belongs to a course, and each attendance row is unique for a session and a student. Status is `PRESENT`, `ABSENT_JUSTIFIED`, or `ABSENT_UNJUSTIFIED`.

Two rules can hold a result for an administration decision:

1. Three unjustified absences in the same course, during the current semester, block that course's exams.
2. When 20% or more of the student's attendance records in the current semester are unjustified, the finals of every course are blocked. Midterms and projects stay validated.

Fall is September through January. Spring is February through August. A blocked grade is stored with `PENDING_ADMIN_DECISION` and a reason, and the computed course grade has `finalGrade = null`. A teacher or an admin clears the block with `PUT /attendance/{id}/status`. A student role answers 403. A blocked grade counts as failed for Change 1 prerequisites.

### How many classes were changed

**31 Java classes** — 26 production and 5 test classes, plus this README:

- Production added: `AttendanceStatus`, `GradeStatus`, `SemesterCalendar`, `AbsencePolicy`, `Session`, `Attendance`, `SessionRepository`, `AttendanceRepository`, `AbsenceRuleService`, `SessionService`, `AttendanceService`, `SessionRequest`, `SessionResponse`, `AttendanceRequest`, `AttendanceStatusRequest`, `AttendanceResponse`, `SessionController`, `AttendanceController`.
- Production updated: `Grade`, `GradeResponse`, `CourseGradeResponse`, `PrerequisitePolicy`, `GradeService`, `CourseGradeService`, `StudentService`, `CourseService`.
- Tests added: `SemesterCalendarTest`, `AbsencePolicyTest`.
- Tests updated: `StudentServiceTest`, `CourseServiceTest`, `GradeServiceTest`.

### Functionalities affected

- New session and attendance endpoints, including duplicate rejection (409) and role-checked status changes.
- Recording a grade stores the number, then marks it pending when a rule blocks it.
- Course grade calculation keeps the averages and sets `finalGrade` to null while the student is blocked.
- Deleting a student or a course removes attendance and session rows first.

### Regression tests needed

**5 new tests** (27 → **32 total**): fall and spring boundaries, the two absence thresholds, and a blocked grade that no longer counts as passed. `mvn test` does not need MySQL.

### Development and validation time

About 2 hours, including a full `mvn test` run on JDK 17.

### Unrelated functionality that broke

Deleting a course or a student failed once sessions and attendances added foreign keys. Both delete methods now remove those rows first. A blocked numeric grade of 10 or more would also have been treated as passed by the prerequisite rule; `Grade.countsAsPassed` ignores `PENDING_ADMIN_DECISION`.

## Changes 4 — Waiting list and automatic promotion on cancellation

A full course no longer rejects the student. `POST /registrations` answers **201** with the registration when a seat is free, and **202** with a waiting-list entry when it is not. Capacity still counts only `VALIDATED` registrations. Create, cancel, and leaving the list lock the course row so two requests cannot take the last seat.

Cancelling a registration promotes the oldest waiting student who is still eligible. A student who is already registered, or who no longer satisfies the prerequisites, stays on the list and the next student is tried. Positions are computed from insertion order.

### How many classes were changed

**12 Java classes** — 9 production and 3 test classes, plus this README:

- Production added: `WaitingListEntry`, `WaitingListRepository`, `WaitingListResponse`, `Enrollment`.
- Production updated: `CourseRepository`, `CourseService`, `StudentService`, `RegistrationService`, `RegistrationController`.
- Tests updated: `RegistrationServiceTest`, `StudentServiceTest`, `CourseServiceTest`.

### Functionalities affected

- Registration creation returns a seat or a FIFO waiting-list place. A duplicate registration or a duplicate waiting-list entry answers 409.
- `DELETE /registrations/{id}` cancels a registration and promotes the first eligible waiting student.
- `GET /registrations/waiting-list?courseId=` lists entries with a computed position.
- `DELETE /registrations/waiting-list/{id}` lets a student leave the list.
- Deleting a course or a student removes waiting-list rows first.

### Regression tests needed

The full-course rejection test was replaced by a waiting-list test. The suite stays at **32 tests** and does not need MySQL. Existing delete tests still run against the cleanup path.

### Development and validation time

About 1 hour 30 minutes, including a full `mvn test` run on JDK 17.

### Unrelated functionality that broke

Deleting a course or a student failed once `waiting_list` added foreign keys. Both delete methods now remove those rows before the parent row.

## Changes 5 — Multi-channel notifications with preferences

Students can be notified by email, SMS, or WhatsApp. `NotificationService` is the only place that sends. It reads the student's channel preferences, asks a `ChannelSender` to deliver, and stores every attempt as `SENT` or `FAILED`. A missing phone number or a sender error is recorded and does not fail the registration, grade, or session request. Email is the default when the student has no preference row.

Notifications go out when a registration is accepted (including a waiting-list promotion), when a session is created or moved, and when a grade is posted. A blocked grade says that an administration decision is pending.

### How many classes were changed

**20 Java classes** — 16 production and 4 test classes, plus this README:

- Production added: `NotificationChannel`, `NotificationType`, `NotificationStatus`, `NotificationPreference`, `Notification`, `ChannelSender`, `LoggingChannelSender`, `NotificationRepository`, `NotificationPreferenceRepository`, `NotificationService`, `NotificationPreferenceRequest`, `NotificationPreferenceResponse`, `NotificationResponse`.
- Production updated: `Student`, `StudentRequest`, `StudentResponse`, `StudentService`, `RegistrationService`, `RegistrationRepository`, `SessionService`, `SessionController`, `GradeService`, `StudentController`.
- Tests added: `LoggingChannelSenderTest`.
- Tests updated: `StudentServiceTest`, `RegistrationServiceTest`, `GradeServiceTest`, `StudentControllerTest`.

### Functionalities affected

- `GET` and `PUT /students/{id}/notification-preferences` choose any mix of EMAIL, SMS, and WHATSAPP.
- `Student` gained an optional `phoneNumber`, used by SMS and WhatsApp.
- `PUT /sessions/{id}` changes the date and label, refuses a course change, and notifies validated students.
- `GET /students/{id}/notifications` returns attempts, newest first.
- Deleting a student also deletes notification rows and preferences.

### Regression tests needed

**1 new test** (32 → **33 total**) checks that a blank SMS destination fails that channel. The existing registration, grade, and student tests still pass with the new collaborators. `mvn test` does not need MySQL.

### Development and validation time

About 2 hours, including a full `mvn test` run on JDK 17.

### Unrelated functionality that broke

Deleting a student failed once notifications referenced the student. `StudentService.delete` now removes notification and preference rows first.

## Changes 6 — Externalized exams

Exams are created and read through an external REST service. This API keeps a local mirror so grades still use a stable numeric id, old exams stay readable during an outage, and course averages and prerequisite checks keep working. `external.exams.base-url` defaults to `http://localhost:9090`, with a 2 second connect timeout and a 3 second read timeout.

A network error, timeout, or HTTP 5xx is unavailable: a write answers **503** and stores nothing, and a read answers from the local mirror. An HTTP 4xx or a bad payload answers **502**. If the external service returns 404 for a known mirror, the mirror is marked `CANCELLED` and kept. Once an exam has grades, a refresh updates only its external id and status. The date, course, and category stay frozen.

### How many classes were changed

**16 Java classes** — 14 production and 2 test classes, plus this README and `application.properties`:

- Production added: `ExamStatus`, `RemoteExam`, `RemoteExamDraft`, `ExamCatalog`, `RestExamCatalog`, `ExternalExamProperties`, `ExternalExamConfig`, `ExamMirror`.
- Production updated: `Exam`, `ExamResponse`, `ExamRepository`, `GradeRepository`, `CourseRepository`, `ExamService`.
- Tests added: `ExamMirrorTest`, `ExamCatalogFallbackTest`.

### Functionalities affected

- `POST /exams` creates the exam externally first, then stores the mirror.
- `GET /exams`, `GET /exams/{id}`, and `GET /exams?courseId=` refresh the mirror, and fall back to it when the external service is down.
- Legacy exams with no external id are never sent out.
- Categories map to `MIDTERM`, `FINAL`, and `PROJECT`. Statuses map to `ACTIVE`, `CANCELLED`, or `UNKNOWN`.

### Regression tests needed

**3 new tests** (33 → **36 total**): category and status mapping, a graded mirror that keeps its date, and a read that falls back to the local mirror. `mvn test` does not need MySQL or a running external API.

### Development and validation time

About 2 hours, including a full `mvn test` run on JDK 17.

### Unrelated functionality that broke

Refreshing a mirrored exam would have changed the date and category that course grades and prerequisite checks already used. A graded mirror now freezes those fields.

## Changes 7 — Academic is a logical name

Registration asks Academic whether the student is actively enrolled before it locks the course. The call uses `RestTemplate`, and the business code only knows the name `academic`.

The starting point was a single physical address, `http://localhost:8082`, written into the registration logic. A host or port change then required a code change. Several Academic instances could not be reached, and a frontend that learned those addresses would depend on the internal layout.

The instances now live in configuration:

```properties
discovery.services.academic[0]=http://localhost:8082
discovery.services.academic[1]=http://localhost:8083
```

Each registration starts at the next instance. A connection failure or HTTP 5xx tries the following instance. If every instance is down, the API answers **503** with `Academic service is unavailable` and does not include the host or the port. HTTP 404 is an answer from Academic (`400`, student unknown) and does not move on. An inactive enrollment is **400** and does not create a registration or a waiting-list entry.

Academic is expected at `GET {instance}/students/{id}/enrollment` with JSON `{"studentId":1,"active":true,"program":"CS"}`.

The frontend only calls this API on port 8080. Registration and error payloads do not carry Academic's address.

### How many classes were changed

**11 Java classes** — 8 production and 3 test classes, plus this README, `application.properties`, and the test workflow:

- Production added: `AcademicClient`, `AcademicEnrollment`, `RestTemplateAcademicClient`, `AcademicClientConfig`, `ServiceDirectory`, `DiscoveryProperties`, `ConfiguredServiceDirectory`.
- Production updated: `RegistrationService`.
- Tests added: `RestTemplateAcademicClientTest`, `ConfiguredServiceDirectoryTest`.
- Tests updated: `RegistrationServiceTest`.

### Functionalities affected

- `POST /registrations` checks Academic before prerequisites, capacity, and the waiting list.
- An inactive or unknown Academic enrollment is **400**. Every instance down is **503**, and nothing is stored.
- Changing Academic's host or port is a configuration change. Adding an instance is another `discovery.services.academic` entry.

### Regression tests needed

**7 new tests** (36 → **43 total**): round-robin across two instances, failover, a 503 whose message hides the physical address, a 404 that does not try the next instance, an inactive student, configuration that drops a trailing slash, and a registration that stops before the course lock. The older registration tests use an Academic client that reports the student as active. `mvn test` does not need MySQL or a running Academic service.

### Development and validation time

About 2 hours, including a full `mvn test` run on JDK 17.

### Unrelated functionality that broke

The Academic call sat inside the course lock at first, so a slow or down instance would have held the course row. The check now runs after the student is loaded and before `loadForUpdate`. Cancel, promotion, grades, and exams do not call Academic.

## Model

- User (id, name, role) is the parent of Student (email, program, optional phone number). Joined inheritance uses the tables `users` and `students`.
- Course (code, title, capacity, required courses, equivalent courses, midterm weight 30, final weight 50, project weight 20).
- Registration (student, course, status). A student can register for a course only once, only when Academic reports an active enrollment, and only when every prerequisite was passed in the previous academic year. A full course places the student on a FIFO waiting list.
- WaitingListEntry (course, student, requestedAt). Position is computed, not stored.
- Exam (course, date, type `MIDTERM`, `FINAL`, or `PROJECT`, external id, status `ACTIVE`, `CANCELLED`, or `UNKNOWN`).
- Grade (exam, student, value from 0 to 20, status `VALIDATED` or `PENDING_ADMIN_DECISION`, block reason). One grade per student and exam.
- Session (course, date, label).
- Attendance (session, student, status). One row per session and student.
- NotificationPreference (student, email enabled by default, SMS, WhatsApp).
- Notification (student, type, channel, status, message, recipient, reference, failure reason, created at).

## Endpoints

| Method | Endpoint | Result |
|---|---|---|
| POST | /students | 201 |
| GET | /students, /students/{id} | 200 / 404 |
| PUT | /students/{id} | 200 / 404 / 409 |
| DELETE | /students/{id} | 204 / 404 |
| GET | /students/{id}/registrations | 200 / 404 |
| GET | /students/{id}/grades | 200 / 404 |
| GET | /students/{id}/notification-preferences | 200 / 404 |
| PUT | /students/{id}/notification-preferences | 200 / 404 |
| GET | /students/{id}/notifications | 200 / 404 |
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
| POST | /registrations | 201 (registered) / 202 (waiting list) / 400 / 409 / 503 |
| DELETE | /registrations/{id} | 204 / 404 |
| GET | /registrations/waiting-list?courseId={id} | 200 / 404 |
| DELETE | /registrations/waiting-list/{id} | 204 / 404 |
| POST | /exams | 201 / 400 / 502 / 503 |
| GET | /exams, /exams/{id} | 200 / 404 (local mirror if the external service is down) |
| GET | /exams?courseId={id} | 200 (local mirror if the external service is down) |
| POST | /grades | 201 / 400 / 409 |
| POST | /sessions | 201 / 400 |
| GET | /sessions, /sessions/{id} | 200 / 404 |
| GET | /sessions?courseId={id} | 200 |
| PUT | /sessions/{id} | 200 / 400 / 404 |
| POST | /attendance | 201 / 400 / 409 |
| GET | /attendance, /attendance/{id} | 200 / 404 |
| GET | /attendance?studentId={id} | 200 |
| PUT | /attendance/{id}/status | 200 / 403 / 404 |

## Quick test

    curl -X POST localhost:8080/students -H "Content-Type: application/json" -d "{\"name\":\"Mohamed Alaoui\",\"email\":\"m@example.com\",\"program\":\"Genie Informatique\"}"
    curl -X POST localhost:8080/courses -H "Content-Type: application/json" -d "{\"code\":\"CS101\",\"title\":\"Algorithms\",\"capacity\":30}"
    curl -X POST localhost:8080/registrations -H "Content-Type: application/json" -d "{\"studentId\":1,\"courseId\":1}"
    curl -X POST localhost:8080/exams -H "Content-Type: application/json" -d "{\"courseId\":1,\"date\":\"2026-12-20\"}"
    curl -X POST localhost:8080/grades -H "Content-Type: application/json" -d "{\"examId\":1,\"studentId\":1,\"value\":15.5}"
    curl localhost:8080/students/1/grades

`POST /registrations` also needs Academic. By default it tries `http://localhost:8082` and then `http://localhost:8083`, at `GET /students/{id}/enrollment`. The caller still uses port 8080 only. If both instances are down, the registration answers 503 and stores nothing.
