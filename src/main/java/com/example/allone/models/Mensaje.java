package com.example.allone.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenido;
    private String tipo; // texto, imagen o video
    private LocalDateTime created_at;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false) // 🔴 Define la clave foránea correctamente
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
