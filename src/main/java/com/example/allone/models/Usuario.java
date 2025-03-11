package com.example.allone.models;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private String password;
    private String avatar;
    private LocalDateTime created_at;
}
