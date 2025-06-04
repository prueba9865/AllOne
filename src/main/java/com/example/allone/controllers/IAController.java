package com.example.allone.controllers;

import com.example.allone.DTO.ChatRequestDTO;
import com.example.allone.models.ChatIA;
import com.example.allone.services.OpenRouterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class IAController {

    private final OpenRouterService svc;

    public IAController(OpenRouterService svc) {
        this.svc = svc;
    }

    @PostMapping(path = "/chat/{usuarioId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chat(
            @RequestBody ChatRequestDTO req,  // Usa el DTO en lugar de la entidad
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(svc.chat(req.getContenido(), usuarioId));
    }
}



