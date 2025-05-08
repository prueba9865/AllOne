package com.example.allone.services;

import com.example.allone.DTO.UsuarioDTO;
import com.example.allone.DTO.UsuarioEditDTO;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDTO obtenerUsuarioParaEditar(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .avatar(usuario.getAvatar())
                .tipo("local") // Asumimos que es local para edición
                .build();
    }

    public Usuario actualizarUsuario(Long usuarioId, UsuarioEditDTO usuarioEditDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar contraseña antigua (usando PasswordEncoder)
        if (usuarioEditDTO.getAntiguaPassword() != null && !usuarioEditDTO.getAntiguaPassword().isEmpty()) {
            if (!passwordEncoder.matches(usuarioEditDTO.getAntiguaPassword(), usuario.getPassword())) {
                throw new RuntimeException("La contraseña antigua no coincide con la actual");
            }
        }

        // Validar que las nuevas contraseñas coincidan
        if (usuarioEditDTO.getPassword() != null && !usuarioEditDTO.getPassword().isEmpty()) {
            if (!usuarioEditDTO.getPassword().equals(usuarioEditDTO.getPassword2())) {
                throw new RuntimeException("Las nuevas contraseñas no coinciden");
            }
            usuario.setPassword(passwordEncoder.encode(usuarioEditDTO.getPassword()));
        }

        // Validar email único
        if (!usuario.getEmail().equals(usuarioEditDTO.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioEditDTO.getEmail())) {
                throw new RuntimeException("El email ya está en uso");
            }
            usuario.setEmail(usuarioEditDTO.getEmail());
        }

        usuario.setNombre(usuarioEditDTO.getNombre());
        usuario.setAvatar(usuarioEditDTO.getAvatar());

        return usuarioRepository.save(usuario);
    }

    public ResponseEntity<Map<String,String>> eliminarUsuario(Long usuarioId){
        if(!usuarioRepository.findById(usuarioId).isPresent()){
            return ResponseEntity.ok(Map.of("error", "El ID no existe"));
        }
        usuarioRepository.deleteById(usuarioId);
        return ResponseEntity.ok(Map.of("success", "Usuario eliminado correctamente"));
    }
}
