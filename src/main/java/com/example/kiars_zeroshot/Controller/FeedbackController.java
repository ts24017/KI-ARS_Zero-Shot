package com.example.kiars_zeroshot.Controller;

import com.example.kiars_zeroshot.DTO.*;
import com.example.kiars_zeroshot.Entities.FeedbackEntity;
import com.example.kiars_zeroshot.Entities.LectureEntity;
import com.example.kiars_zeroshot.Entities.UserEntity;
import com.example.kiars_zeroshot.Repositories.FeedbackRepository;
import com.example.kiars_zeroshot.Repositories.LectureRepository;
import com.example.kiars_zeroshot.Repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackRepository feedbackRepo;
    private final LectureRepository lectureRepo;
    private final UserRepository userRepo;
    private final RestTemplate restTemplate;

    public FeedbackController(FeedbackRepository feedbackRepo, LectureRepository lectureRepo, UserRepository userRepo, RestTemplate restTemplate) {
        this.feedbackRepo = feedbackRepo;
        this.lectureRepo = lectureRepo;
        this.userRepo = userRepo;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/lecture/{lectureId}")
    public List<FeedbackResponse> getFeedbackForLecture(@PathVariable Long lectureId) {
        LectureEntity lecture = lectureRepo.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        return feedbackRepo.findByLecture(lecture).stream()
                .map(fb -> new FeedbackResponse(
                        fb.getId(),
                        fb.getText(),
                        fb.getSentiment(),
                        fb.getTopic(),
                        fb.isQuestion(),
                        fb.getUrgency(),
                        lecture.getDate().toString(),
                        lecture.getCourse().getName(),
                        fb.getStudent() != null ? fb.getStudent().getId() : null // 🔹 hier neu
                ))
                .toList();
    }

    @GetMapping("/lecture/{lectureId}/summary")
    public FeedbackAggregationResponse getFeedbackSummaryForLecture(@PathVariable Long lectureId) {
        LectureEntity lecture = lectureRepo.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        var feedbackList = feedbackRepo.findByLecture(lecture);
        long total = feedbackList.size();

        Map<String, Long> sentimentCounts = feedbackList.stream()
                .collect(Collectors.groupingBy(fb -> fb.getSentiment(), Collectors.counting()));

        Map<String, Long> topicCounts = feedbackList.stream()
                .collect(Collectors.groupingBy(fb -> fb.getTopic(), Collectors.counting()));

        long questionCount = feedbackList.stream().filter(FeedbackEntity::isQuestion).count();
        long statementCount = total - questionCount;

        Map<String, Long> urgencyCounts = feedbackList.stream()
                .filter(FeedbackEntity::isQuestion)
                .collect(Collectors.groupingBy(fb -> fb.getUrgency(), Collectors.counting()));

        return new FeedbackAggregationResponse(
                lecture.getId(),
                lecture.getDate().toString(),
                lecture.getCourse().getName(),
                total,
                sentimentCounts,
                topicCounts,
                questionCount,
                statementCount,
                urgencyCounts
        );
    }

    @GetMapping("/course/{courseId}")
    public List<FeedbackResponse> getFeedbackForCourse(@PathVariable Long courseId) {
        var lectures = lectureRepo.findAll().stream()
                .filter(l -> l.getCourse().getId().equals(courseId))
                .toList();

        return lectures.stream()
                .flatMap(l -> feedbackRepo.findByLecture(l).stream()
                        .map(fb -> new FeedbackResponse(
                                fb.getId(),
                                fb.getText(),
                                fb.getSentiment(),
                                fb.getTopic(),
                                fb.isQuestion(),
                                fb.getUrgency(),
                                l.getDate().toString(),
                                l.getCourse().getName(),
                                fb.getStudent() != null ? fb.getStudent().getId() : null // 🔹 neu
                        )))
                .toList();
    }

    @GetMapping("/course/{courseId}/summary")
    public FeedbackAggregationResponse getFeedbackSummaryForCourse(@PathVariable Long courseId) {
        var lectures = lectureRepo.findAll().stream()
                .filter(l -> l.getCourse().getId().equals(courseId))
                .toList();

        var feedbackList = lectures.stream()
                .flatMap(l -> feedbackRepo.findByLecture(l).stream())
                .toList();

        long total = feedbackList.size();

        Map<String, Long> sentimentCounts = feedbackList.stream()
                .collect(Collectors.groupingBy(
                        fb -> fb.getSentiment(),
                        Collectors.counting()
                ));

        Map<String, Long> topicCounts = feedbackList.stream()
                .collect(Collectors.groupingBy(
                        fb -> fb.getTopic(),
                        Collectors.counting()
                ));

        long questionCount = feedbackList.stream().filter(fb -> fb.isQuestion()).count();
        long statementCount = total - questionCount;

        Map<String, Long> urgencyCounts = feedbackList.stream()
                .filter(fb -> fb.isQuestion())
                .collect(Collectors.groupingBy(
                        fb -> fb.getUrgency(),
                        Collectors.counting()
                ));

        String courseName = lectures.isEmpty() ? "Unbekannt" : lectures.get(0).getCourse().getName();

        return new FeedbackAggregationResponse(
                null,
                null,
                courseName,
                total,
                sentimentCounts,
                topicCounts,
                questionCount,
                statementCount,
                urgencyCounts
        );



}

    @PostMapping("/lecture/{lectureId}")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long lectureId,
            @RequestBody FeedbackRequest request
    ) {
        LectureEntity lecture = lectureRepo.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        UserEntity student = userRepo.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        FeedbackResult result = restTemplate.postForObject(
                "http://localhost:8000/classify",
                request,
                FeedbackResult.class
        );

        if (result == null || result.getSentiment() == null) {
            return ResponseEntity.badRequest().body("Fehler bei der KI-Auswertung");
        }

        FeedbackClassification fc = new FeedbackClassification();
        fc.setText(result.getText());
        fc.setSentiment(result.getSentiment().getLabel());
        fc.setQuestion(result.getQuestion().isQuestion());
        fc.setUrgency(result.getQuestion().getUrgency());
        fc.setTopic(result.getTopics().getLabels().get(0).getLabel());

        FeedbackEntity entity = new FeedbackEntity(fc);
        entity.setLecture(lecture);
        entity.setStudent(student);
        feedbackRepo.save(entity);

        return ResponseEntity.ok("Feedback erfolgreich gesendet und gespeichert.");
    }

}






