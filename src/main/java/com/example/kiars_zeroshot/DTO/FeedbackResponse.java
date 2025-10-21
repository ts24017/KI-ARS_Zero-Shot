package com.example.kiars_zeroshot.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private String text;
    private String sentiment;
    private String topic;
    private boolean question;
    private String urgency;
    private String lectureDate;
    private String courseName;
    private Long studentId;
}

