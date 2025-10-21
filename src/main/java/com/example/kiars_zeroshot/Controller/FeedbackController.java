package com.example.kiars_zeroshot.Controller;

import com.example.kiars_zeroshot.DTO.FeedbackAggregationResponse;
import com.example.kiars_zeroshot.DTO.FeedbackResponse;
import com.example.kiars_zeroshot.Entities.FeedbackEntity;
import com.example.kiars_zeroshot.Entities.LectureEntity;
import com.example.kiars_zeroshot.Repositories.FeedbackRepository;
import com.example.kiars_zeroshot.Repositories.LectureRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackRepository feedbackRepo;
    private final LectureRepository lectureRepo;

    public FeedbackController(FeedbackRepository feedbackRepo, LectureRepository lectureRepo) {
        this.feedbackRepo = feedbackRepo;
        this.lectureRepo = lectureRepo;
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

}






