    package com.example.allone.DTO;

    import jakarta.persistence.Column;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.Pattern;
    import jakarta.validation.constraints.Size;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class UserRegisterDTO {

        @NotBlank(message = "El nombre no puede estar en blanco")
        @Pattern(regexp = "^[A-Za-zñÑáéíóúÁÉÍÓÚ ]{1,50}$", message = "El nombre solo puede contener letras y espacios")
        private String nombre;

        @Column(unique = true)
        @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Introduce un email válido")
        @NotBlank(message = "El email no puede estar en blanco")
        private String email;

        @Column(unique = true)
        @NotBlank(message = "El usuario no puede estar en blanco")
        @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,20}$", message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos")
        private String username;

        @Pattern(regexp = "^\\+\\d{1,2}\\s\\d{7,12}$", message = "Formato erróneo del número de teléfono")
        @NotBlank(message = "El número de teléfono no puede estar en blanco")
        private String telefono;

        private String password;
        private String password2;
        private String avatar;
    }
