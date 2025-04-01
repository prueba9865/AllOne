package com.example.allone.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false) // 🔴 Define la clave foránea correctamente
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "usuario_google_id", nullable = false)
    private UsuarioGoogle usuarioGoogle;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS); // Truncado a segundos
    }
}
