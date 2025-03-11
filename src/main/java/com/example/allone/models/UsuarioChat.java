package com.example.allone.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rol;
    private LocalDateTime joined_at;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false) // 🔴 Define la clave foránea
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
