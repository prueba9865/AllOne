package com.example.allone.controllers;

import com.example.allone.DTO.ChatIADTO;
import com.example.allone.services.OpenRouterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class IAController {

    private final OpenRouterService svc;

    public IAController(OpenRouterService svc) {
        this.svc = svc;
    }

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatIADTO req) {
        return ResponseEntity.ok(svc.chat(req.getContenido()));
    }
}



