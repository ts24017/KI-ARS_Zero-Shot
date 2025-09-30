package com.example.kiars_zeroshot.Testdaten;

import com.example.kiars_zeroshot.DTO.FeedbackClassification;
import com.example.kiars_zeroshot.DTO.FeedbackRequest;
import com.example.kiars_zeroshot.DTO.FeedbackResult;
import com.example.kiars_zeroshot.Entities.CourseEntity;
import com.example.kiars_zeroshot.Entities.FeedbackEntity;
import com.example.kiars_zeroshot.Entities.LectureEntity;
import com.example.kiars_zeroshot.Repositories.CourseRepository;
import com.example.kiars_zeroshot.Repositories.FeedbackRepository;
import com.example.kiars_zeroshot.Repositories.LectureRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            CourseRepository courseRepo,
            LectureRepository lectureRepo,
            FeedbackRepository feedbackRepo,
            RestTemplate restTemplate) {
        return args -> {

            // Kurs anlegen
            CourseEntity course = new CourseEntity();
            course.setName("Informatikrecht");
            courseRepo.save(course);

            // Vorlesungstermine anlegen
            LectureEntity lecture1 = new LectureEntity();
            lecture1.setCourse(course);
            lecture1.setDate(LocalDate.of(2025, 7, 7));
            lectureRepo.save(lecture1);

            LectureEntity lecture2 = new LectureEntity();
            lecture2.setCourse(course);
            lecture2.setDate(LocalDate.of(2025, 7, 14));
            lectureRepo.save(lecture2);

            // Feedback-Texte vorbereiten
            List<String> feedbackTexts1 = List.of(
                    // Positiv
                    "Die Struktur der Inhalte hat mir sehr geholfen.",
                    "Die Erklärungen waren sehr gut.",
                    "Die Vorlesung war insgesamt gut organisiert.",
                    "Die Theorie wurde nachvollziehbar vermittelt.",
                    "Der rote Faden in der Vorlesung war deutlich erkennbar.",
                    "Ich konnte dem Aufbau der Vorlesung gut folgen.",
                    "Die Inhalte waren interessant und spannend.",
                    "Die Wiederholungen haben mein Verständnis verbessert.",
                    "Die Gliederung der Vorlesung war sehr übersichtlich.",
                    "Die Beispiele waren praxisnah und haben geholfen.",

                    // Negativ
                    "Manchmal waren die Erklärungen zu oberflächlich.",
                    "Die Inhalte waren sehr detailliert, aber schwer zu behalten.",
                    "Es gab zu viele Folien pro Sitzung, das war etwas unübersichtlich.",
                    "Die Vorlesung war spannend, aber etwas zu schnell.",
                    "Es wurden viele Fachbegriffe genutzt, die nicht erklärt wurden.",
                    "Mir hat die klare Gliederung nicht gefallen.",
                    "Die Inhalte waren zu umfangreich für die verfügbare Zeit.",
                    "Manchmal wurde zu lange bei Details verweilt.",
                    "Manche Abschnitte wirkten chaotisch.",
                    "Mir hat ein Überblick am Anfang gefehlt.",

                    // Neutral / gemischt
                    "Könnten Sie das morgen in der Vorlesung erklären?",
                    "Könnten Sie die wichtigsten Inhalte am Ende noch einmal zusammenfassen?",
                    "Ich hätte mir mehr Diskussionen im Plenum gewünscht.",
                    "Die Länge der Vorlesung war in Ordnung.",
                    "Das Tempo war manchmal passend, manchmal etwas schnell.",
                    "Die Folien waren informativ, aber teilweise überladen.",
                    "Die Theorie wurde erklärt, Praxisbeispiele hätte ich mir mehr gewünscht.",
                    "Manche Themen waren sehr interessant, andere weniger.",
                    "Die Atmosphäre in der Vorlesung war angenehm.",
                    "Ich hätte mir mehr Interaktion mit den Studierenden gewünscht.",

                    // Weitere Variationen
                    "Die Zusammenfassungen am Ende waren hilfreich.",
                    "Die Vorlesung hätte strukturierter beginnen können.",
                    "Die Inhalte waren überwiegend gut verständlich.",
                    "Es wurde zu viel Wert auf Details gelegt.",
                    "Die Beispiele im Skript waren sehr gut.",
                    "Die Wiederholungen fand ich manchmal unnötig.",
                    "Es war insgesamt gut verständlich.",
                    "Die Inhalte waren interessant, aber zu theoretisch.",
                    "Die Gliederung war klar, aber manchmal etwas starr.",
                    "Die Folien waren schön gestaltet, aber zu textlastig.",

                    // Mehr Fragen
                    "Könnten Sie bitte nächste Woche ein Beispiel mehr durchrechnen?",
                    "Könnten Sie die wichtigsten Begriffe genauer definieren?",
                    "Wäre es möglich, am Ende eine Fragerunde einzubauen?",
                    "Könnten Sie die Themen mit mehr Praxisbezug erklären?",
                    "Wird es eine Wiederholung vor der Klausur geben?",
                    "Könnten Sie die zentralen Inhalte noch einmal zusammenfassen?",
                    "Könnten Sie bitte die Folien vorher hochladen?",
                    "Wäre es möglich, mehr Zeit für Fragen einzuplanen?",
                    "Könnten Sie den Stoff etwas langsamer durchgehen?",
                    "Könnten Sie Beispiele aus der Praxis ergänzen?",

                    // Letzte Ergänzungen
                    "Die Gruppendiskussionen waren sehr hilfreich.",
                    "Die Inhalte waren spannend, aber nicht immer klar erklärt.",
                    "Die Folien waren hilfreich, aber manchmal unübersichtlich.",
                    "Die Theorie wurde gut erklärt, aber Praxis fehlte.",
                    "Die Wiederholungen am Anfang waren sinnvoll."
            );



            List<String> feedbackTexts2 = List.of(
                    "Die Übungen waren hilfreich und gut nachvollziehbar.",
                    "Bitte laden Sie die Folien spätestens einen Tag vorher hoch.",
                    "Die Materialien kamen oft zu spät.",
                    "Die Übungen haben das Verständnis gefördert.",
                    "Ich hätte mir mehr Übungsaufgaben gewünscht.",
                    "Es wäre hilfreich, wenn die Folien früher verfügbar wären.",
                    "Die Aufgaben in den Übungen waren praxisnah.",
                    "Die Übungen haben Spaß gemacht.",
                    "Leider waren die Materialien nicht immer vollständig.",
                    "Ich konnte mit den Übungen mein Wissen vertiefen.",
                    "Manchmal waren die Übungsaufgaben zu schwer.",
                    "Die Folien kamen teilweise erst nach der Vorlesung.",
                    "Die Übungen haben gut zur Vorlesung gepasst.",
                    "Bitte stellen Sie die Materialien konsistenter bereit.",
                    "Die Übungen waren abwechslungsreich und interessant.",
                    "Ohne die Folien konnte ich mich schlecht vorbereiten.",
                    "Die Übungen waren sehr hilfreich, um den Stoff zu wiederholen.",
                    "Es gab zu wenige Beispiele in den Übungen.",
                    "Wenn die Folien früher da wären, könnte man besser folgen.",
                    "Könnten Sie mehr Übungsaufgaben mit Lösungen bereitstellen?"
            );


            // Feedback für Termin 1 klassifizieren
            for (String text : feedbackTexts1) {
                FeedbackRequest req = new FeedbackRequest();
                req.setText(text);

                FeedbackResult result = restTemplate.postForObject(
                        "http://localhost:8000/classify", req, FeedbackResult.class);

                FeedbackClassification fc = new FeedbackClassification();
                fc.setText(result.getText());
                fc.setSentiment(result.getSentiment().getLabel());
                fc.setQuestion(result.getQuestion().isQuestion());
                fc.setUrgency(result.getQuestion().getUrgency());
                fc.setTopic(result.getTopics().getLabels().get(0).getLabel()); // nur erstes Topic nehmen

                FeedbackEntity entity = new FeedbackEntity(fc);
                entity.setLecture(lecture1);
                feedbackRepo.save(entity);
            }

            // Feedback für Termin 2 klassifizieren
            for (String text : feedbackTexts2) {
                FeedbackRequest req = new FeedbackRequest();
                req.setText(text);

                FeedbackResult result = restTemplate.postForObject(
                        "http://localhost:8000/classify", req, FeedbackResult.class);

                FeedbackClassification reduced = new FeedbackClassification();
                reduced.setText(text);
                reduced.setSentiment(result.getSentiment().getLabel());
                reduced.setQuestion(result.getQuestion().isQuestion());
                reduced.setUrgency(result.getQuestion().getUrgency());
                reduced.setTopic(result.getTopics().getLabels().get(0).getLabel());

                FeedbackEntity entity = new FeedbackEntity(reduced);
                entity.setLecture(lecture2);
                feedbackRepo.save(entity);
            }

            System.out.println("Testdaten geladen.");
        };
    }
}

