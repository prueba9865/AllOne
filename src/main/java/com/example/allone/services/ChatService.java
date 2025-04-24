package com.example.allone.services;

import com.example.allone.models.Chat;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.ChatRepository;
import com.example.allone.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepo;
    private final UsuarioRepository usuarioRepo;

    @Transactional
    public Chat getOrCreateIndividualChat(Long userId, Long contactoId) {
        // 1) intento buscar uno existente (en cualquiera de los dos órdenes)
        return chatRepo
                .findIndividualChatBetween(userId, contactoId)
                .or(() -> chatRepo.findIndividualChatBetween(contactoId, userId))
                .orElseGet(() -> {
                    // 2) si no existe, crear
                    Usuario u1 = usuarioRepo.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
                    Usuario u2 = usuarioRepo.findById(contactoId)
                            .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado"));

                    Chat chat = Chat.builder()
                            .tipo("individual")
                            .participantes(Set.of(u1, u2))
                            .build();
                    return chatRepo.save(chat);
                });
    }
}
