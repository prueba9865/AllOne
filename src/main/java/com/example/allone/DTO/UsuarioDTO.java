package com.example.allone.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;

    @NotBlank(message = "El nombre no puede estar en blanco")
    @Pattern(regexp = "^[A-Za-zñÑáéíóúÁÉÍÓÚ ]{1,50}$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @Column(unique = true)
    @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Introduce un email válido")
    @NotBlank(message = "El email no puede estar en blanco")
    private String email;
    private String avatar;
    private String tipo; // "local" o "google" o cualquier otra plataforma de login
}
