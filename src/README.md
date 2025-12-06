# KI-ARS Backend

## Quick Start

1. Navigate to the project root:

       cd KI-ARS_Zero-Shot

**Note:** Make sure your terminal is located in the directory where you cloned the project (e.g., Downloads or Desktop).

2. Start the backend using the Maven wrapper:


   (macOS / Linux):

        ./mvnw spring-boot:run       

(Windows)

         mvnw.cmd spring-boot:run    

The backend will be available at http://localhost:8080

---

## Initial test data

On application startup, the backend loads initial test data into the in-memory H2 database.  
This includes a lecturer, courses, lectures and sample feedback entries.

During startup you should see a log line similar to:

    Testdaten erfolgreich geladen: Dozent, Kurse, Vorlesungen & Feedback.

The sample feedback is automatically classified via the AI service (if it is running) and can be inspected through the frontend dashboards or via the REST API.

---

## API Endpoints

### Feedback

| Method | Endpoint                               | Description                                                  |
|--------|----------------------------------------|--------------------------------------------------------------|
| GET    | `/api/feedback/lecture/{lectureId}`    | Returns all feedback entries for a given lecture.           |
| GET    | `/api/feedback/lecture/{lectureId}/summary` | Returns aggregated statistics for a lecture (sentiment, topics, questions, urgency). |
| GET    | `/api/feedback/course/{courseId}`      | Returns all feedback entries for all lectures of a course.  |
| GET    | `/api/feedback/course/{courseId}/summary` | Returns aggregated statistics for all lectures of a course. |
| POST   | `/api/feedback/lecture/{lectureId}`    | Submits new feedback for a lecture and triggers AI analysis.|

The POST endpoint calls the AI service at `http://localhost:8000/classify`, stores the classified feedback in the H2 database and returns a confirmation message.

---

### Courses and lectures

| Method | Endpoint                            | Description                                      |
|--------|-------------------------------------|--------------------------------------------------|
| GET    | `/api/course/{courseId}/lectures`  | Returns all lectures belonging to a course.     |

---

### Student and teacher views

| Method | Endpoint                                   | Description                                      |
|--------|--------------------------------------------|--------------------------------------------------|
| GET    | `/api/student/{studentId}/courses`         | Returns all courses a given student is enrolled in. |
| GET    | `/api/teacher/{teacherId}/courses`         | Returns all courses taught by a given teacher.     |

---

## Notes

- The backend expects the AI service to be running at `http://localhost:8000`.
- By default an in-memory H2 database is used; data is reset on every restart.
- The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:testdb`, user `sa`, empty password).


