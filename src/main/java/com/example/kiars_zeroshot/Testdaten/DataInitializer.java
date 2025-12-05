package com.example.kiars_zeroshot.Testdaten;

import com.example.kiars_zeroshot.DTO.FeedbackClassification;
import com.example.kiars_zeroshot.DTO.FeedbackRequest;
import com.example.kiars_zeroshot.DTO.FeedbackResult;
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

            UserEntity dozent = new UserEntity();
            dozent.setUsername("Dr. Herrmann");
            dozent.setPassword(passwordEncoder.encode("1234"));
            dozent.setRole(Role.DOZENT);

            UserEntity student1 = new UserEntity();
            student1.setUsername("Max");
            student1.setPassword(passwordEncoder.encode("1234"));
            student1.setRole(Role.STUDENT);

            UserEntity student2 = new UserEntity();
            student2.setUsername("Laura");
            student2.setPassword(passwordEncoder.encode("1234"));
            student2.setRole(Role.STUDENT);

            UserEntity student3 = new UserEntity();
            student3.setUsername("Tobias");
            student3.setPassword(passwordEncoder.encode("1234"));
            student3.setRole(Role.STUDENT);

            userRepo.saveAll(List.of(dozent, student1, student2, student3));
            List<UserEntity> students = List.of(student1, student2, student3);

            CourseEntity course1 = new CourseEntity();
            course1.setName("Informatikrecht");
            course1.setTeacher(dozent);
            course1.setStudents(students);

            CourseEntity course2 = new CourseEntity();
            course2.setName("Datenbanken und Algorithmen");
            course2.setTeacher(dozent);
            course2.setStudents(List.of(student1, student3));

            dozent.getTaughtCourses().addAll(List.of(course1, course2));

            LectureEntity lecture1 = new LectureEntity();
            lecture1.setDate(LocalDate.of(2025, 4, 10));
            lecture1.setCourse(course1);
            course1.getLectures().add(lecture1);

            LectureEntity lecture2 = new LectureEntity();
            lecture2.setDate(LocalDate.of(2025, 5, 3));
            lecture2.setCourse(course1);
            course1.getLectures().add(lecture2);

            LectureEntity lecture3 = new LectureEntity();
            lecture3.setDate(LocalDate.of(2025, 7, 21));
            lecture3.setCourse(course1);
            course1.getLectures().add(lecture3);

            LectureEntity lecture4 = new LectureEntity();
            lecture4.setDate(LocalDate.of(2025, 3, 15));
            lecture4.setCourse(course2);
            course2.getLectures().add(lecture4);

            LectureEntity lecture5 = new LectureEntity();
            lecture5.setDate(LocalDate.of(2025, 4, 20));
            lecture5.setCourse(course2);
            course2.getLectures().add(lecture5);

            courseRepo.saveAll(List.of(course1, course2));
            lectureRepo.saveAll(List.of(lecture1, lecture2, lecture3, lecture4, lecture5));
            userRepo.save(dozent);

            List<String> feedbackLecture1 = List.of(
                    "Könnten Sie bitte nochmal genau sagen, ob die Vorlesung übermorgen online oder in Präsenz stattfindet?",
                    "Beginnt die Vorlesung eigentlich morgen schon um 8 Uhr oder doch erst um 08:30 Uhr?",
                    "Können Sie bitte den Raum nennen, wo die Vorlesung morgen stattfinden wird?",
                    "Wäre es möglich, zu Beginn jeder Vorlesung kurz den Ablauf für die heutige Sitzung zu präsentieren? Das würde den Einstieg sehr erleichtern.",
                    "Einteilung der Gruppenarbeiten in der heutigen Vorlesung war ziemlich chaotisch. Könnte das beim nächsten Male bessere organisiet werden?",
                    "Der Aufbau der Vorlesung heute war echt top.",
                    "Die Agenda am Anfang der heutigen Sitzung hat mir sehr geholfen, den Überblick zu behalten.",
                    "Leide hat die Vorlesung heute wieder 10 Minuten später aufgehört, sodass ich mich zur Folgevorlesung versptet habe. ",
                    "Der Reihenfolge der Themen in der Volrsung heute hat null zu der Gliederung gepasst, die online hochgeladen war.",
                    "Die Fragerunde und Diskussion am Ende fand ich wirklich perfekt umgesetzt.",
                    "Fande die Recap-Einheit zu Beginn gestern in der Vorlesung toll.",
                    "Ich fande den Aufbau der Vorlesung heute sehr monton.",
                    "Ich finde es langsam echt toll, dass wir wieder 20 Minuten verspätet begonnen haben.",
                    "Der rote Faden der Vorlesung war stellenweise nicht erkennbar, manche Themen hätten auch einfach zusammengefasst werden können. Das hätte das Ganze nicht so unnötig in die Länge gezogen heute.",
                    "Ich fand den Inhalt heute mega spannend",
                    "Die Wiederholung am Anfang der Stunde war echt gut, um wieder reinzukommen.",
                    "Ich hätte mir zu den Themen heute zum Verstehen mehr anschauliche Beispiele gewünscht.",
                    "Die Inhalte waren sehr komplex, ich konnte kaum folgen.",
                    "Mir war heute der Praxisbezug überhaupt nicht klar, ich weiß nicht, wofür ich das später brauchen kann.",
                    "Besonders hilfreich fand ich heute die vielen Fremdwörter ohne Erklärung. Das regt super zum Selbststudium an.",
                    "Ich fand den Inhalt von der Vorlesung gestern ehrlich gesagt ziemlich schlecht vermittelt. Habe wenig mitgenommen.",
                    "Es wurde in der Vorlesung mit so vielen Fachbegriffen ohne Erklärung herumgeworfen, dass ich völlig verwirrt war am Ende."
            );

            // Informatikrecht – Lecture 2 (Blockchain / Big Data / Smart Contracts etc.)
            List<String> feedbackLecture2 = List.of(
                    "Da wir in wenigen Tagen mit der ersten Abgabe starten, könnten Sie bitte kurz die genauen Fristen nennen?",
                    "Der Übergang von der Theorie zum Praxisteil war heute sehr abrupt.",
                    "Die Erklärung zum ARIS-Haus war letztes Mal schon hilfreich, könnten Sie dazu vielleicht trotzdem morgen noch ein Beispiel bringen?",
                    "Die Beispiele zur Blockchain Thema waren super spannend, könnten wir die morgen nochmal kurz vertiefen?",
                    "Könnten Sie vielleicht noch ein Praxisbeispiel zu Big Data bringen. Fand ich persönlich sehr spannend.",
                    "Könnten Sie die Folie mit den Smart Contracts übermrgen wiederholen?",
                    "Könnte man demnächst vielleicht eine kleine Zusammenfassung bekommen, damit man versteht wie die einzelnen Kapitel zusammenhängen?",
                    "Wird bald gezeigt wie das Ganze auch in der Praxis aussieht",
                    "Endlich mal eine Vorlesung, wo man wirklich checkt, wofür man den ganzen Stoff später mal braucht.",
                    "Können Sie uns Lösungen für die Übungsaufgaben zum Skript für die gestrige Vorlesung die Tage hochladen? Das wäre eine enorme Bereichung beim Lernen!",
                    "Die Übungsaufgabe zum letzten Kapitel hat enorm geholfen. Könnten Sie bei der kommenden Vorlesung noch ein ähnliches Beispiel machen?"
            );

            List<String> feedbackLecture3 = List.of(
                    "Ist es möglich die Unterschiede zwischen Datenschutz und Datensicherheit beim nächsten Mal nochmal klar zu erläutern?",
                    "Ich hab das Konzept der DSGVO letztes Mal nicht geschnallt. Könnten Sie das morgen bitte nochmal erklären?",
                    "Könnten Sie nächste Woche bitte noch einmal die Definitionen durchgehen, damit es auch für alle eindeutig ist?",
                    "Wann wird die Aufzeichnung zur dieser Vorlesung hochgeladen?",
                    "Können Sie die Lösungen für die Probeklausur schon morgen im Kurs verfügbar machen? Das wäre extrem hilfreich fürs Lernen.",
                    "Wird die Aufzeichnung von letzter Woche schon morgen im Kurs verfügbar sein?",
                    "Könnten sie das Skript heute Abend noch hochladen?",
                    "Wo finde ich die Aufzeichnung zu der heutigen Vorlesung",
                    "Die Unterlagen zu dieser Vorlesung fehlen immer noch, könnten Sie das bitte demnächst nachholen?",
                    "Welches Kapitel von diesem Skript ist wichtig für die Klausur",
                    "Die Kommentare in den Folien sind eine große Hilfe.",
                    "Das Skript ist super gestaltet.",
                    "Die Unterlagen zu Vorlesung sind leider seit Tagen nicht im ILIAS-Kurs verfügbar.",
                    "Die Grafik auf Folie 7 ist echt hervorragend.",
                    "Die Zusammenfassung am Ende der Folien war hilfreich, um den Überblick zu behalten.",
                    "Die Zusatzliteraturhinweise  in den Vorlesungsunterlagen waren echt sehr spannend und haben das Thema gut ergänzt.",
                    "Die Links im Vorlesungsskripts funktionieren nicht.",
                    "Die Aufzeichnung ist leider unvollständig.",
                    "Die hochgeladenen Unterlagen sind teilweise einfach leer.",
                    "Die Grafiken waren zu klein und kaum zu erkennen.",
                    "Wird das Thema Datenschutz in der kommenden Vorlesung nochmal aufgegriffen, um beim Stoff dranbleiben zu können?",
                    "Könnte man ab nächster Woche immer eine kurze Pause (5 Minuten) einführen.",
                    "Wird der Stoff dieser Vorlesungseinheit auch übermorgen in der Klausur abgefragt oder ist das zusätzlich zum Lernpensum?",
                    "Bleibt das demnächt so mit der Menge an Stoff pro Woche?",
                    "Wie viel Zeit sollten wir ungefähr pro Woche für die Vor- und Nachbereitung einplanen?",
                    "Wird die Stoffdichte in der Vorlesung übermorgen ähnlich hoch sein wie letzte Woche?",
                    "Die Stoffdichte in den Vorlesungen fand ich bisher ziemlich angenehm. Bleibt das auch so oder wirds doch schon ab morgen intensiver?",
                    "Können Sie die Anzahl an Übungsaufgaben vielleicht reduzieren? Bin aktuell ziemlich überfordert.",
                    "Ich komme kaum mit beim Mitschreiben, wäre es daher möglich kurze Pausen zwischen den Themen einzulegen? ",
                    "Finde den Aufwand zum Lernen bislang sehr angemessen. Hoffe das bleibt auch so?",
                    "Man kommt zeitlich kaum mit der Stoffmenge hinterher.",
                    "Die Stoffverteilung ist sehr angenehm. ",
                    "Die Vorlesung heute war echt heftig anstrengend. So viele komplexe Themen auf einmal.",
                    "Man hat echt das Gefühl, dass alles ziemlich gedrängt ist.",
                    "Die Menge an Themen pro Vorlesung ist genau richtig. Man kann gut mitarbeiten.",
                    "Heute war’s ja richtig entspannt… also wenn man Marathonlernen mag.",
                    "Fand das Pensum gestern genau richtig, weder zu viel noch zu wenig.",
                    "Gegen Ende der Vorlesung wurde es schon etwas viel auf einmal.",
                    "Ich fand den Stoffumfang bisher pro Vorlesung sehr angenehm, man kommt immer gut mit.",
                    "Ich finde, die Vorlesung hat aktuell ein mega angenehmes Tempolevel."
            );

            List<String> feedbackLecture4 = List.of(
                    "Findet jetzt die nächste Übungseinheit überhaupt statt oder doch nicht?",
                    "Ist eigentlich schon klar in welchem Raum die erste Übung nächste Woche stattfinden wird?",
                    "Wo bekommt man die Formelsammlung für die Übung nächste Woche?",
                    "Können Sie die Lösungen für das letzte Übungsblatt bereitstellen?",
                    "Werden wir in diesem Semester eigentlich auch mit der Statistik Software arbeiten?"
            );


            List<String> feedbackLecture5 = List.of(
                    "Wäre es möglich, Raumänderungen künftig per Mail anzukünden? Das würde uns wirklich helfen.",
                    "Könnten mögliche Raumänderungen ab nächster Woche bitte etwas früher mitgeteilt werden? Vielen Dank!",
                    "Die Internet Verbindung war heute wirklich katastrophal.",
                    "Die Laptops im Vorlesungssaal sind sehr modern.",
                    "Der Beamer im Raum war heute sehr unscharf.",
                    "Noch nie so eine schlechte Audio-Qualität in einer Vorlesungsaufzeichnung gehört.",
                    "Fand es richtig top, dass wir heute das digitale Whiteboard nutzen konnten. Das hat die Zusammenarbeit echt erleichtert.",
                    "Endlich gabs heute im Saal genug Steckdosen.",
                    "Man kann das vielleicht irgendwie mit dem Mikrofon regeln, dass man auch die Antworten der Studenten in der Aufzeichnung hört?",
                    "Wäre es möglich in Zukunft kurz zu prüfen, ob die Audioqualität überall im Saal passt?",
                    "Wird das mit der Aufnahme nächste Woche wieder funktionieren?",
                    "Könnten Sie bitte schauen, ob der Ton beim nächsten Mal etwas lauter ist?",
                    "Könnte man vielleicht zukünftig prüfen, ob das Beamerbild etwas klarer eingestellt werden kann.",
                    "Könnte das Problem mit dem Zugangsfehler zum ILIAS-Kurs bitte bis morgen behoben werden?",
                    "Ich frage mich ob die Vorlesung ab morgen endlich wieder aufgezeichnet werden kann.",
                    "Gibt es eine Möglichkeit, das Problem mit dem Mikrofon bis zur Vorlesung morgen zu beheben?",
                    "Könnte man das Licht beim nächsten Mal ein wenig anpassen?",
                    "Die Bambus Leitung im Hörsaal vorgestern war hervorragend.",
                    "Ich finde die doppelte Wiedergabegeschwindigkeit bei den Vorlesungsaufzeichnungen enorm zeitsparend.",
                    "Während der Präsentation ist mehrmals etwas ausgefallen, das war ziemlich nervig.",
                    "Das Equipment im Raum macht einen sehr guten Eindruck."
            );

            Random random = new Random();

            java.util.function.BiConsumer<List<String>, LectureEntity> saveFeedbackForLecture =
                    (texts, lecture) -> {
                        for (String text : texts) {
                            FeedbackRequest req = new FeedbackRequest();
                            req.setText(text);

                            FeedbackResult result = restTemplate.postForObject(
                                    "http://localhost:8000/classify", req, FeedbackResult.class);

                            if (result == null || result.getSentiment() == null) continue;

                            FeedbackClassification fc = new FeedbackClassification();
                            fc.setText(result.getText());
                            fc.setSentiment(result.getSentiment().getLabel());
                            fc.setQuestion(result.getQuestion().isQuestion());
                            fc.setUrgency(result.getQuestion().getUrgency());
                            fc.setTopic(result.getTopics().getLabels().get(0).getLabel());

                            FeedbackEntity entity = new FeedbackEntity(fc);
                            entity.setLecture(lecture);
                            entity.setStudent(students.get(random.nextInt(students.size())));
                            feedbackRepo.save(entity);
                        }
                    };

            saveFeedbackForLecture.accept(feedbackLecture1, lecture1);
            saveFeedbackForLecture.accept(feedbackLecture2, lecture2);
            saveFeedbackForLecture.accept(feedbackLecture3, lecture3);
            saveFeedbackForLecture.accept(feedbackLecture4, lecture4);
            saveFeedbackForLecture.accept(feedbackLecture5, lecture5);

            System.out.println("Testdaten erfolgreich geladen: Dozent, Kurse, Vorlesungen & Feedback.");
        };
    }
}

