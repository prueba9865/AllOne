package com.example.allone.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeDTO {
    private String contenido;
    private String tipo;        // "texto", "imagen", etc.
    private Long usuarioId;     // tu id de Usuario
    private Long usuarioGoogleId; // o null si usas sólo usuarios internos
}
