package com.example.kiars_zeroshot.Controller;

import com.example.kiars_zeroshot.DTO.CourseResponse;
import com.example.kiars_zeroshot.Repositories.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final UserRepository userRepo;

    public TeacherController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/{teacherId}/courses")
    public List<CourseResponse> getTeacherCourses(@PathVariable Long teacherId) {
        var teacher = userRepo.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return teacher.getTaughtCourses().stream()
                .map(course -> new CourseResponse(
                        course.getId(),
                        course.getName(),
                        teacher.getUsername()
                ))
                .collect(Collectors.toList());
    }
}
