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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public ResponseEntity<Map<String,String>> eliminarMensaje(Long messageId) {
        // Verificar si el usuario existe
        Optional<Mensaje> mensajeOptional = repo.findById(messageId);
        if(!mensajeOptional.isPresent()) {
            return ResponseEntity.ok(Map.of("error", "El ID no existe"));
        }

        Mensaje mensaje = mensajeOptional.get();

        repo.deleteById(messageId);
        return ResponseEntity.ok(Map.of("success", "Mensaje eliminado correctamente"));
    }

    public Mensaje updateContenido(Long id, String nuevoContenido) {
        Mensaje msg = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mensaje no encontrado: " + id));
        msg.setContenido(nuevoContenido);
        return repo.save(msg);
    }
}
