package com.example.allone.controllers;

import com.example.allone.DTO.MensajeDTO;
import com.example.allone.models.Mensaje;
import com.example.allone.services.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/{usuarioId}/{contactoId}/messages")
public class MensajeController {
    @Autowired
    private MensajeService service;

    @GetMapping
    public List<Mensaje> getMensajesByUsuario(@PathVariable Long usuarioId, @PathVariable Long contactoId) {  // Cambia el parámetro
        return service.listarMensajesByUsuario(usuarioId, contactoId);  // Cambia el método del servicio
    }

    @PostMapping
    public ResponseEntity<Mensaje> postMensaje(
            @PathVariable Long usuarioId,
            @RequestBody MensajeDTO dto) {
        Mensaje creado = service.enviarMensaje(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}

