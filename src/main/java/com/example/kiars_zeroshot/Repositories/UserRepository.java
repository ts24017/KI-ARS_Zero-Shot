package com.example.kiars_zeroshot.Repositories;

import com.example.kiars_zeroshot.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

        Optional<UserEntity> findByUsername(String username);

}
