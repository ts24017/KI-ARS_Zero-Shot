package com.example.kiars_zeroshot.Entities;

import com.example.kiars_zeroshot.DTO.FeedbackClassification;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private String sentiment;
    private String topic;
    private boolean question;
    private String urgency;


    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserEntity student;


    public FeedbackEntity(FeedbackClassification fc) {
        this.text = fc.getText();
        this.sentiment = fc.getSentiment();
        this.topic = fc.getTopic();
        this.question = fc.isQuestion();
        this.urgency = fc.getUrgency();
    }

}
