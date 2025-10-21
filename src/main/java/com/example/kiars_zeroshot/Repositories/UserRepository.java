package com.example.kiars_zeroshot.Repositories;

import com.example.kiars_zeroshot.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
