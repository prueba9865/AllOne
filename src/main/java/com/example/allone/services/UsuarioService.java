package com.example.allone.services;

import com.example.allone.DTO.UsuarioDTO;
import com.example.allone.DTO.UsuarioEditDTO;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    // Servicio
    public Usuario actualizarUsuario(Long usuarioId, UsuarioEditDTO dto ,String nombreArchivo) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1) Verificación de antiguaPassword
        if (dto.getAntiguaPassword() != null && !dto.getAntiguaPassword().isEmpty()) {
            if (!passwordEncoder.matches(dto.getAntiguaPassword(), u.getPassword())) {
                throw new RuntimeException("La contraseña antigua no coincide");
            }
        }

        if(passwordEncoder.matches(dto.getPassword(), u.getPassword())){
            throw new RuntimeException("La contraseña nueva tiene que ser diferente a la actual");
        }

        // 2) Cambio de contraseña
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!dto.getPassword().equals(dto.getPassword2())) {
                throw new RuntimeException("Las nuevas contraseñas no coinciden");
            }
            u.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 3) Validación de email
        if (!u.getEmail().equals(dto.getEmail()) &&
                usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }
        u.setEmail(dto.getEmail());

        // Validar username único
        if (!u.getUsername().equals(dto.getUsername())) {
            if (usuarioRepository.existsByUsername(dto.getUsername())) {
                throw new RuntimeException("El nombre de usuario ya está en uso");
            }
            u.setUsername(dto.getUsername());
        }

        // 4) Resto de campos
        u.setNombre(dto.getNombre());
        u.setAvatar(nombreArchivo);   // aquí ya es solo el nombre de fichero
        // u.setTipo(dto.getTipo());     // si lo necesitas

        return usuarioRepository.save(u);
    }

    public Usuario actualizarUsuarioParcial(Long usuarioId, UsuarioEditDTO dto, String nombreArchivo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Actualizar solo los campos que vienen en el DTO
        if (dto.getNombre() != null) {
            usuario.setNombre(dto.getNombre());
        }

        if (dto.getEmail() != null) {
            usuario.setEmail(dto.getEmail());
        }

        if (dto.getUsername() != null) {
            usuario.setUsername(dto.getUsername());
        }

        if (nombreArchivo != null) {
            usuario.setAvatar(nombreArchivo);
        }

        // Manejo especial para contraseña
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!passwordEncoder.matches(dto.getAntiguaPassword(), usuario.getPassword())) {
                throw new SecurityException("La contraseña actual no es correcta");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }


    public ResponseEntity<Map<String,String>> eliminarUsuario(Long usuarioId) {
        // Verificar si el usuario existe
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
        if(!usuarioOptional.isPresent()) {
            return ResponseEntity.ok(Map.of("error", "El ID no existe"));
        }

        Usuario usuario = usuarioOptional.get();

        try {
            // Eliminar el avatar si existe
            String avatarPath = usuario.getAvatar();
            if(avatarPath != null && !avatarPath.isEmpty()) {
                // Extraer el nombre del archivo de la URL
                String fileName = avatarPath.substring(avatarPath.lastIndexOf("/") + 1);

                // Construir la ruta completa en el servidor
                Path filePath = Paths.get("uploads/avatars").resolve(fileName).toAbsolutePath();

                // Eliminar el archivo
                Files.deleteIfExists(filePath);
            }

            // Eliminar el usuario
            usuarioRepository.deleteById(usuarioId);

            return ResponseEntity.ok(Map.of("success", "Usuario eliminado correctamente"));
        } catch (IOException e) {
            // Si hay error al eliminar el archivo, igual eliminamos el usuario
            usuarioRepository.deleteById(usuarioId);
            return ResponseEntity.ok(Map.of("warning", "Usuario eliminado pero no se pudo borrar su avatar: " + e.getMessage()));
        }
    }
}
