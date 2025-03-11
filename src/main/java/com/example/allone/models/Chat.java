package com.example.allone.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // individual o grupo
    private String nombre_grupo;
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true) // 🔴 mappedBy debe coincidir con la relación en Mensaje
    private List<Mensaje> mensajes = new ArrayList<>();
}
