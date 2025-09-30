package com.example.kiars_zeroshot;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF-Schutz aus für APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // API freigeben
                        .anyRequest().permitAll()              // alles freigeben (nur für Entwicklung)
                )
                .formLogin(form -> form.disable()) // Login-Formular deaktivieren
                .httpBasic(httpBasic -> httpBasic.disable()); // Basic Auth deaktivieren

        return http.build();
    }
}


