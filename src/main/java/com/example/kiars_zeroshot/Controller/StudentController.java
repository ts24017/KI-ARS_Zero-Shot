package com.example.kiars_zeroshot.Controller;

import com.example.kiars_zeroshot.DTO.CourseResponse;
import com.example.kiars_zeroshot.Entities.CourseEntity;
import com.example.kiars_zeroshot.Entities.UserEntity;
import com.example.kiars_zeroshot.Repositories.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final UserRepository userRepo;

    public StudentController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/{studentId}/courses")
    public List<CourseResponse> getStudentCourses(@PathVariable Long studentId) {
        UserEntity student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return student.getEnrolledCourses().stream()
                .map(this::convertToCourseResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse convertToCourseResponse(CourseEntity course) {
        String teacherName = (course.getTeacher() != null)
                ? course.getTeacher().getUsername()
                : "Unbekannt";
        return new CourseResponse(course.getId(), course.getName(), teacherName);
    }
}

