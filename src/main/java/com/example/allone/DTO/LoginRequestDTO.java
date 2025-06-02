package com.example.allone.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {
    @NotBlank(message = "El usuario no puede estar en blanco")
    private String username;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    private String password;
}
