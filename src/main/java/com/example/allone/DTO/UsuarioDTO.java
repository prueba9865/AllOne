package com.example.allone.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioDTO {
    private Object id; // Puede ser Long o String
    private String nombre;
    private String email;
    private String avatar;
    private String tipo; // "local" o "google"
}
