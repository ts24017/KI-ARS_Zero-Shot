package com.example.kiars_zeroshot.DTO;

import lombok.Data;

@Data
public class FeedbackClassification {
    private String text;
    private String sentiment;
    private String topic;
    private boolean question;
    private String urgency;
}



