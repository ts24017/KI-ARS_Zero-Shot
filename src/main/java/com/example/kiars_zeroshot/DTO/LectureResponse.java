package com.example.kiars_zeroshot.DTO;

public class LectureResponse {
    private Long id;
    private String date;

    public LectureResponse(Long id, String date) {
        this.id = id;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
