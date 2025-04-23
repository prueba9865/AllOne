package com.example.allone.services;

import com.example.allone.DTO.MensajeDTO;
import com.example.allone.models.Chat;
import com.example.allone.models.Mensaje;
import com.example.allone.models.Usuario;
import com.example.allone.models.UsuarioGoogle;
import com.example.allone.repositories.ChatRepository;
import com.example.allone.repositories.MensajeRepository;
import com.example.allone.repositories.UsuarioGoogleRepository;
import com.example.allone.repositories.UsuarioRepository;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensajeService {
    @Autowired private MensajeRepository repo;
    @Autowired private ChatRepository chatRepo;
    @Autowired private UsuarioRepository userRepo;
    @Autowired private UsuarioGoogleRepository gUserRepo;

    public List<Mensaje> listarMensajes(Long chatId) {
        return repo.findByChatIdOrderByCreatedAtAsc(chatId);
    }

    public Mensaje enviarMensaje(Long chatId, MensajeDTO dto) {
        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat no existe"));
        Usuario u = userRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no existe"));
        UsuarioGoogle ug = gUserRepo.findById(dto.getUsuarioGoogleId())
                .orElseThrow(() -> new EntityNotFoundException("UsuarioGoogle no existe"));

        Mensaje m = Mensaje.builder()
                .chat(chat)
                .usuario(u)
                .usuarioGoogle(ug)
                .contenido(dto.getContenido())
                .tipo(dto.getTipo())
                .build();
        return repo.save(m);
    }
}
