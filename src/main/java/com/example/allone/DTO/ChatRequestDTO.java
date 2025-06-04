package com.example.allone.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// En el paquete com.example.allone.DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequestDTO {
    private String contenido;  // Solo campos necesarios para la petición
}