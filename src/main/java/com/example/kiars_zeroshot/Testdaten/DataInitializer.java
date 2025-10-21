package com.example.kiars_zeroshot.Testdaten;

import com.example.kiars_zeroshot.DTO.*;
import com.example.kiars_zeroshot.Entities.*;
import com.example.kiars_zeroshot.Repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            CourseRepository courseRepo,
            LectureRepository lectureRepo,
            FeedbackRepository feedbackRepo,
            UserRepository userRepo,
            RestTemplate restTemplate,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            // === 👨‍🏫 Dozent anlegen ===
            UserEntity dozent = new UserEntity();
            dozent.setUsername("strobel");
            dozent.setPassword(passwordEncoder.encode("1234"));
            dozent.setRole(Role.DOZENT);

            // === 👨‍🎓 Studierende anlegen ===
            UserEntity student1 = new UserEntity();
            student1.setUsername("max");
            student1.setPassword(passwordEncoder.encode("1234"));
            student1.setRole(Role.STUDENT);

            UserEntity student2 = new UserEntity();
            student2.setUsername("laura");
            student2.setPassword(passwordEncoder.encode("1234"));
            student2.setRole(Role.STUDENT);

            UserEntity student3 = new UserEntity();
            student3.setUsername("tobias");
            student3.setPassword(passwordEncoder.encode("1234"));
            student3.setRole(Role.STUDENT);

            userRepo.saveAll(List.of(dozent, student1, student2, student3));
            List<UserEntity> students = List.of(student1, student2, student3);

            // === 📘 Kurse anlegen ===
            CourseEntity course1 = new CourseEntity();
            course1.setName("Informatikrecht");
            course1.setTeacher(dozent);
            course1.setStudents(students);

            CourseEntity course2 = new CourseEntity();
            course2.setName("Datenbanken und Algorithmen");
            course2.setTeacher(dozent);
            course2.setStudents(List.of(student1, student3));

            // 🔹 beidseitig verknüpfen
            dozent.getTaughtCourses().addAll(List.of(course1, course2));

            // === 🧾 Vorlesungen hinzufügen ===
            LectureEntity lecture1 = new LectureEntity();
            lecture1.setDate(LocalDate.of(2025, 4, 10));
            lecture1.setCourse(course1);
            course1.getLectures().add(lecture1); // 🔹 beidseitig verknüpfen

            LectureEntity lecture2 = new LectureEntity();
            lecture2.setDate(LocalDate.of(2025, 5, 3));
            lecture2.setCourse(course1);
            course1.getLectures().add(lecture2); // 🔹 beidseitig verknüpfen

            LectureEntity lecture3 = new LectureEntity();
            lecture3.setDate(LocalDate.of(2025, 7, 21));
            lecture3.setCourse(course1);
            course1.getLectures().add(lecture3); // 🔹 beidseitig verknüpfen

            LectureEntity lecture4 = new LectureEntity();
            lecture4.setDate(LocalDate.of(2025, 3, 15));
            lecture4.setCourse(course2);
            course2.getLectures().add(lecture4); // 🔹 beidseitig verknüpfen

            // 🔹 Alles speichern
            courseRepo.saveAll(List.of(course1, course2));
            lectureRepo.saveAll(List.of(lecture1, lecture2, lecture3, lecture4));
            userRepo.save(dozent);

            // === 💬 Beispiel-Feedbacks hinzufügen ===
            List<String> feedbackTexts = List.of(
                    "Könnten Sie bitte nochmal genau sagen, ob die Vorlesung übermorgen online oder in Präsenz stattfindet?",
                    "Der Aufbau der Vorlesung heute war echt top.",
                    "Die Fragerunde am Ende fand ich super.",
                    "Ich fand den Inhalt heute mega spannend."
            );

            Random random = new Random();

            for (String text : feedbackTexts) {
                FeedbackRequest req = new FeedbackRequest();
                req.setText(text);

                FeedbackResult result = restTemplate.postForObject(
                        "http://localhost:8000/classify", req, FeedbackResult.class);

                if (result == null || result.getSentiment() == null) continue; // 🔹 Absicherung

                FeedbackClassification fc = new FeedbackClassification();
                fc.setText(result.getText());
                fc.setSentiment(result.getSentiment().getLabel());
                fc.setQuestion(result.getQuestion().isQuestion());
                fc.setUrgency(result.getQuestion().getUrgency());
                fc.setTopic(result.getTopics().getLabels().get(0).getLabel());

                FeedbackEntity entity = new FeedbackEntity(fc);
                entity.setLecture(lecture1);
                entity.setStudent(students.get(random.nextInt(students.size())));
                feedbackRepo.save(entity);
            }

            System.out.println("✅ Testdaten erfolgreich geladen: Dozent, Kurse, Vorlesungen & Feedback.");
        };
    }
}

