package com.example.allone.services;

import com.example.allone.DTO.MensajeDTO;
import com.example.allone.models.Mensaje;
import com.example.allone.models.Usuario;
import com.example.allone.models.UsuarioGoogle;
import com.example.allone.repositories.MensajeRepository;
import com.example.allone.repositories.UsuarioGoogleRepository;
import com.example.allone.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensajeService {
    @Autowired private MensajeRepository repo;
    @Autowired private UsuarioRepository userRepo;
    @Autowired private UsuarioGoogleRepository gUserRepo;

    public List<Mensaje> listarMensajesByUsuario(Long usuarioId, Long contactoId) {
        return repo.findConversacionBetweenUsers(usuarioId, contactoId);  // Nuevo método del repositorio
    }

    public Mensaje enviarMensaje(Long usuarioId, MensajeDTO dto) {
        Usuario u = userRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario/emisor no existe"));
        Usuario c = userRepo.findById(dto.getContactoId())
                .orElseThrow(() -> new EntityNotFoundException("Remitente no existente"));

        Mensaje m = Mensaje.builder()
                .usuario(u)
                .contacto(c)
                .contenido(dto.getContenido())
                .tipo(dto.getTipo())
                .build();
        return repo.save(m);
    }
}
