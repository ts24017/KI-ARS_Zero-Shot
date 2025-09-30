package com.example.kiars_zeroshot.DTO;

import java.util.Map;

public class FeedbackAggregationResponse {
    private Long lectureId;
    private String lectureDate;
    private String courseName;

    private long totalFeedback;
    private Map<String, Long> sentimentCounts;
    private Map<String, Long> topicCounts;
    private long questionCount;
    private long statementCount;
    private Map<String, Long> urgencyCounts;  // 👈 NEU

    public FeedbackAggregationResponse() {}

    public FeedbackAggregationResponse(Long lectureId, String lectureDate, String courseName,
                                       long totalFeedback, Map<String, Long> sentimentCounts,
                                       Map<String, Long> topicCounts,
                                       long questionCount, long statementCount,
                                       Map<String, Long> urgencyCounts) {
        this.lectureId = lectureId;
        this.lectureDate = lectureDate;
        this.courseName = courseName;
        this.totalFeedback = totalFeedback;
        this.sentimentCounts = sentimentCounts;
        this.topicCounts = topicCounts;
        this.questionCount = questionCount;
        this.statementCount = statementCount;
        this.urgencyCounts = urgencyCounts;
    }

    public Long getLectureId() { return lectureId; }
    public void setLectureId(Long lectureId) { this.lectureId = lectureId; }

    public String getLectureDate() { return lectureDate; }
    public void setLectureDate(String lectureDate) { this.lectureDate = lectureDate; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public long getTotalFeedback() { return totalFeedback; }
    public void setTotalFeedback(long totalFeedback) { this.totalFeedback = totalFeedback; }

    public Map<String, Long> getSentimentCounts() { return sentimentCounts; }
    public void setSentimentCounts(Map<String, Long> sentimentCounts) { this.sentimentCounts = sentimentCounts; }

    public Map<String, Long> getTopicCounts() { return topicCounts; }
    public void setTopicCounts(Map<String, Long> topicCounts) { this.topicCounts = topicCounts; }

    public long getQuestionCount() { return questionCount; }
    public void setQuestionCount(long questionCount) { this.questionCount = questionCount; }

    public long getStatementCount() { return statementCount; }
    public void setStatementCount(long statementCount) { this.statementCount = statementCount; }

    public Map<String, Long> getUrgencyCounts() { return urgencyCounts; }
    public void setUrgencyCounts(Map<String, Long> urgencyCounts) { this.urgencyCounts = urgencyCounts; }
}


