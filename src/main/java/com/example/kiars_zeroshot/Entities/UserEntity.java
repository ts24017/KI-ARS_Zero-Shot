package com.example.kiars_zeroshot.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // optional
    private String email; // optional, falls du später Login baust

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        STUDENT,
        DOZENT
    }
}
