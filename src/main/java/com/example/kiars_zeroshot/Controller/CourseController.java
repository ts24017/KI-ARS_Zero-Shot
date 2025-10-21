package com.example.kiars_zeroshot.Controller;

import com.example.kiars_zeroshot.DTO.LectureResponse;
import com.example.kiars_zeroshot.Repositories.LectureRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final LectureRepository lectureRepo;

    public CourseController(LectureRepository lectureRepo) {
        this.lectureRepo = lectureRepo;
    }

    @GetMapping("/{courseId}/lectures")
    public List<LectureResponse> getLecturesForCourse(@PathVariable Long courseId) {
        return lectureRepo.findAll().stream()
                .filter(l -> l.getCourse().getId().equals(courseId))
                .map(l -> new LectureResponse(
                        l.getId(),
                        l.getDate().toString()
                ))
                .toList();
    }
}
