package com.example.allone.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UsuarioEditDTO {
    @NotBlank(message = "El nombre no puede estar en blanco")
    @Pattern(regexp = "^[A-Za-zñÑáéíóúÁÉÍÓÚ ]{1,50}$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @Column(unique = true)
    @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Introduce un email válido")
    @NotBlank(message = "El email no puede estar en blanco")
    private String email;

    private String avatar;

    private String antiguaPassword;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    @Size(min = 12, message = "La contraseña debe tener mínimo 12 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
            message = "La contraseña debe tener al menos 12 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    @Size(min = 12, message = "La contraseña debe tener mínimo 12 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
            message = "La contraseña debe tener al menos 12 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password2;

    private String tipo; // "local" o "google"
}
