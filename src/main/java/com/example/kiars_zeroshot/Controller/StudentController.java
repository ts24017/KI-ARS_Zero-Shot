package com.example.kiars_zeroshot.Controller;

import com.example.kiars_zeroshot.Entities.CourseEntity;
import com.example.kiars_zeroshot.Repositories.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final UserRepository userRepo;

    public StudentController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/{studentId}/courses")
    public List<CourseEntity> getStudentCourses(@PathVariable Long studentId) {
        var student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return student.getEnrolledCourses();
    }
}

