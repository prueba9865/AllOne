    package com.example.allone.DTO;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class UserRegisterDTO {
        private String nombre;
        private String email;
        private String username;
        private String password;
        private String password2;
        private String avatar;
    }
