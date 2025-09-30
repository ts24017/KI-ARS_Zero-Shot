package com.example.kiars_zeroshot.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class FeedbackResult {
    private String mode;
    private String text;
    private Sentiment sentiment;
    private Question question;
    private Topics topics;

    @Data
    public static class Sentiment {
        private String label;
        private double score_pos;
        private double score_neg;
    }

    @Data
    public static class Question {
        @JsonProperty("is_question")
        private boolean question;
        private double score_question;
        private double score_statement;
        private boolean heuristic_used;
        private String urgency;
    }


    @Data
    public static class Topics {
        private String applicable_set;
        private List<TopicLabel> labels;
    }

    @Data
    public static class TopicLabel {
        private String label;
        private double score;
    }
}
