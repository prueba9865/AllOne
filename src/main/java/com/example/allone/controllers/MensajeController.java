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
@RequestMapping("/api/chats/{chatId}/messages")
public class MensajeController {
    @Autowired
    private MensajeService service;

    @GetMapping
    public List<Mensaje> getMensajes(@PathVariable Long chatId) {
        return service.listarMensajes(chatId);
    }

    @PostMapping
    public ResponseEntity<Mensaje> postMensaje(
            @PathVariable Long chatId,
            @RequestBody MensajeDTO dto) {
        Mensaje creado = service.enviarMensaje(chatId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}

