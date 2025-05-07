package com.example.allone.DTO;

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
public class MensajeDTO {
    private String contenido;
    private String tipo;        // "texto", "imagen", etc.
    private Long usuarioId;     // tu id de Usuario
    private Long contactoId;
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = this.createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
    //private Long usuarioGoogleId; // o null si usas sólo usuarios internos
}
