package com.example.allone.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ChatIA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenido;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference // ← Anotación clave aquí (evita que se serialice el usuario dentro de mensaje
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS); // Truncado a segundos
    }
}
