package com.example.allone.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference // ← Anotación clave aquí (evita que se serialice el usuario dentro de mensaje
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "contacto_id", nullable = false)
    @JsonBackReference // ← Anotación clave aquí (evita que se serialice el usuario dentro de mensaje
    private Usuario contacto;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS); // Truncado a segundos
    }

    // Expone usuarioId en el JSON para poder obtener y verificar si es "mine" o "their" para los estilos del css y que apareza a la izq o a la derecha el mensaje
    // en el div:
    @JsonProperty("usuarioId")
    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }
}
