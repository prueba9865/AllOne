package com.example.allone.controllers;

import com.example.allone.DTO.LoginRequestDTO;
import com.example.allone.DTO.LoginResponseDTO;
import com.example.allone.DTO.UserRegisterDTO;
import com.example.allone.config.JwtTokenProvider;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
public class UsuarioController {
    private static final List<String> PERMITTED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 10000000;
    private static final String UPLOADS_DIRECTORY = "uploads/avatars";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<Map<String, String>> crearUsuario(@RequestBody @Valid UserRegisterDTO userRegisterDTO,
                                                            @RequestParam("avatar") MultipartFile avatar) {
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getPassword2())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", "Las contraseñas no coinciden")
            );
        }

        guardarFotos(avatar, userRegisterDTO);

        Usuario usuario = this.userRepository.save(
                Usuario.builder()
                        .nombre(userRegisterDTO.getNombre())
                        .username(userRegisterDTO.getUsername())
                        .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                        .email(userRegisterDTO.getEmail())
                        .avatar(userRegisterDTO.getAvatar())
                        .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "Usuario creado exitosamente")
        );
    }


    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> crearTokenUsuario(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        // Validamos al usuario en Spring (hacemos login manualmente)
        UsernamePasswordAuthenticationToken userPassAuthToken =
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());

        Authentication auth = authenticationManager.authenticate(userPassAuthToken); // Puede lanzar BadCredentialsException

        // Obtenemos el usuario autenticado
        Usuario user = (Usuario) auth.getPrincipal();

        // Generamos un token con los datos del usuario
        String token = this.tokenProvider.generateToken(auth);

        // Devolvemos un código 200 con el username y token JWT
        return ResponseEntity.ok(new LoginResponseDTO(user.getId(), user.getUsername(), token));
    }

    public void guardarFotos(MultipartFile foto, UserRegisterDTO userRegisterDTO) {
        // Directorio temporal donde almacenar las fotos antes de redimensionarlas
        Path directorioTemporal = Paths.get(System.getProperty("java.io.tmpdir"));

        if (!foto.isEmpty()) {
            validarArchivo(foto);  // Validar el archivo
            String nombreFoto = generarNombreUnico(foto);  // Generar un nombre único

            // Rutas del archivo temporal y el archivo final
            Path rutaTemporal = directorioTemporal.resolve(nombreFoto);
            Path rutaFinal = Paths.get(UPLOADS_DIRECTORY, nombreFoto);

            try {
                // Guardar el archivo original en el directorio temporal
                foto.transferTo(rutaTemporal.toFile());

                // Redimensionar la imagen y guardarla en la ubicación final
                redimensionarImagen(rutaTemporal.toFile(), rutaFinal.toFile(), 1000);

                // Eliminar el archivo temporal después de redimensionarlo
                Files.deleteIfExists(rutaTemporal);

                userRegisterDTO.setAvatar(nombreFoto);

            } catch (IOException e) {
                throw new RuntimeException("Error al guardar y redimensionar la imagen: " + e.getMessage(), e);
            }
        }
    }

    public static void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo no seleccionado");
        }
        if (!PERMITTED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("El archivo seleccionado no es una imagen.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Archivo demasiado grande. Sólo se admiten archivos < 10MB");
        }
    }

    public static String generarNombreUnico(MultipartFile file) {
        UUID nombreUnico = UUID.randomUUID();
        String extension;
        if (file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
            extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        } else {
            throw new IllegalArgumentException("El archivo seleccionado no es una imagen.");
        }
        return nombreUnico + extension;
    }

    public void redimensionarImagen(File rutaOriginal, File rutaRedimensionada, int ancho) throws IOException {
        Thumbnails.of(rutaOriginal)
                .width(ancho)
                .keepAspectRatio(true)
                .toFile(rutaRedimensionada);
    }
}
