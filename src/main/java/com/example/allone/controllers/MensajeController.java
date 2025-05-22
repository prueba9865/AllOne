package com.example.allone.controllers;

import com.example.allone.DTO.ActualizarMensajeDTO;
import com.example.allone.DTO.MensajeDTO;
import com.example.allone.models.Mensaje;
import com.example.allone.services.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MensajeController {
    @Autowired
    private MensajeService service;

    @GetMapping("/api/user/{usuarioId}/{contactoId}/messages")
    public List<Mensaje> getMensajesByUsuario(@PathVariable Long usuarioId, @PathVariable Long contactoId) {  // Cambia el parámetro
        return service.listarMensajesByUsuario(usuarioId, contactoId);  // Cambia el método del servicio
    }

    @PostMapping("/api/user/{usuarioId}/{contactoId}/messages")
    public ResponseEntity<Mensaje> postMensaje(
            @PathVariable Long usuarioId,
            @RequestBody MensajeDTO dto) {
        Mensaje creado = service.enviarMensaje(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/api/messages/{messageId}")
    public ResponseEntity<?> eliminarMensaje(@PathVariable Long messageId){
        return service.eliminarMensaje(messageId);
    }

    @PatchMapping("/api/messages/{messageId}")
    public ResponseEntity<Mensaje> updateContenido(
            @PathVariable Long messageId,
            @RequestBody ActualizarMensajeDTO request
    ) {
        // Llama al servicio para actualizar solo el contenido
        Mensaje updated = service.updateContenido(messageId, request.getContenido());
        return ResponseEntity.ok(updated);
    }
}

