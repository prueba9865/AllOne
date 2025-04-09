package com.example.allone.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Contacto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;  // Usuario que envía la solicitud

    @ManyToOne
    @JoinColumn(name = "contacto_id")
    private Usuario contacto;  // Usuario que recibe la solicitud

    private boolean aceptado;  // Estado de la solicitud (pendiente/aceptada)
}
