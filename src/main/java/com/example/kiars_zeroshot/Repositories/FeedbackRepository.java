package com.example.kiars_zeroshot.Repositories;

import com.example.kiars_zeroshot.Entities.FeedbackEntity;
import com.example.kiars_zeroshot.Entities.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    List<FeedbackEntity> findByLecture(LectureEntity lecture);
}


