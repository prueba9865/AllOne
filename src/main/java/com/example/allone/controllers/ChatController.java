package com.example.allone.controllers;

import com.example.allone.models.Chat;
import com.example.allone.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/one-on-one")
    public ResponseEntity<Chat> getOrCreateChat(@RequestBody Map<String, Long> body) {
        Long userId    = body.get("usuarioId");
        Long contactoId = body.get("contactoId");
        Chat chat = chatService.getOrCreateIndividualChat(userId, contactoId);
        return ResponseEntity.ok(chat);
    }
}
