package com.example.allone.controllers;

import com.example.allone.DTO.*;
import com.example.allone.config.JwtTokenProvider;
import com.example.allone.errors.ResourceNotFoundException;
import com.example.allone.models.Contacto;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.ContactoRepository;
import com.example.allone.repositories.UsuarioRepository;
import com.example.allone.services.UsuarioService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UsuarioController {
    private static final List<String> PERMITTED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 10485760; // 10 MB en bytes
    private static final String UPLOADS_DIRECTORY = "uploads/avatars";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/decode-jwt")
    public ResponseEntity<?> decodeJwt(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verificar que el header de autorización existe
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authorization header missing or invalid"));
            }

            String jwtToken = authHeader.substring(7); // Eliminar "Bearer "

            // Verificar que el token no esté vacío
            if (jwtToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token is empty"));
            }

            SecretKey key = jwtTokenProvider.claveFirma();

            // Decodificar y verificar el token
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();

            // Verificar claims esenciales
            if (claims.get("id") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token claims"));
            }

            // Construir respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("avatar", "http://localhost:8080/uploads/avatars/" + claims.get("avatar", String.class));
            response.put("id", claims.get("id"));
            response.put("nombre", claims.get("name"));
            response.put("email", claims.get("email"));
            response.put("username", claims.get("username"));

            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token expired", "details", e.getMessage()));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "details", e.getMessage()));
        }
    }

    @GetMapping("/api/v1/usuario/edit/{usuarioId}")
    public ResponseEntity<UsuarioDTO> getUsuarioParaEditar(@PathVariable Long usuarioId) {
        UsuarioDTO usuarioDTO = usuarioService.obtenerUsuarioParaEditar(usuarioId);
        return ResponseEntity.ok(usuarioDTO);
    }

    @PutMapping(value = "/api/v1/usuario/edit/{usuarioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarUsuarioParcial(
            @PathVariable Long usuarioId,
            @ModelAttribute UsuarioEditDTO dto,
            BindingResult result,
            WebRequest request,
            Authentication authentication) {

        // Manejar la imagen si viene en la petición
        String nombreArchivo = null;
        if (dto.getAvatar() != null && !dto.getAvatar().isEmpty()) {
            nombreArchivo = guardarFotos(dto.getAvatar());
            request.setAttribute("nombreArchivo", nombreArchivo, WebRequest.SCOPE_REQUEST);
        }

        // Validar solo campos que vienen en el DTO
        if (dto.getNombre() != null) {
            if (dto.getNombre().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre no puede estar vacío"));
            }
        }

        if (dto.getEmail() != null) {
            // Validar formato email
            if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Formato de email inválido"));
            }
        }

        // Actualizar usuario (solo campos proporcionados)
        Usuario usuarioActualizado = usuarioService.actualizarUsuarioParcial(usuarioId, dto, nombreArchivo);

        // Generar nuevo token
        String nuevoToken = jwtTokenProvider.generateToken(authentication);

        // Construir respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("success", "Usuario actualizado correctamente");
        response.put("token", nuevoToken);

        if (nombreArchivo != null) {
            response.put("avatarUrl", "http://localhost:8080/uploads/avatars/" + usuarioActualizado.getAvatar());
        }

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/api/v1/usuario/delete/{usuarioId}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long usuarioId){
        return usuarioService.eliminarUsuario(usuarioId);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> getUsuariosSimplificados() {
        List<Map<String, Object>> usuariosSimplificados = userRepository.findAll().stream()
                .map(usuario -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", usuario.getId());
                    map.put("name", usuario.getNombre());
                    map.put("username", usuario.getUsername());
                    map.put("avatar", usuario.getAvatar() != null ? "http://localhost:8080/uploads/avatars/" + usuario.getAvatar() : "/default-avatar.png");
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(usuariosSimplificados);
    }

    @PutMapping("/contactos/{idContacto}/aceptar")
    public ResponseEntity<String> aceptarContacto(
            @PathVariable Long idContacto,
            @RequestParam boolean aceptar
    ) {
        Contacto solicitud = contactoRepository.findById(idContacto).orElseThrow();

        if (aceptar) {
            solicitud.setAceptado(true);
            contactoRepository.save(solicitud);
            return ResponseEntity.ok("Contacto agregado");
        } else {
            contactoRepository.delete(solicitud);
            return ResponseEntity.ok("Solicitud rechazada");
        }
    }

    @PutMapping("/solicitudes/{solicitudId}/responder")
    public ResponseEntity<?> responderSolicitud(
            @PathVariable Long solicitudId,
            @RequestParam boolean aceptar) {

        try {
            // Buscar la solicitud
            Contacto solicitud = contactoRepository.findById(solicitudId)
                    .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

            // Procesar la respuesta
            if (aceptar) {
                solicitud.setAceptado(true);
                contactoRepository.save(solicitud);

                return ResponseEntity.ok().body(Map.of(
                        "mensaje", "Solicitud aceptada",
                        "solicitudId", solicitudId
                ));
            } else {
                contactoRepository.delete(solicitud);
                return ResponseEntity.ok().body(Map.of(
                        "mensaje", "Solicitud rechazada",
                        "solicitudId", solicitudId
                ));
            }

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar la solicitud"));
        }
    }

    @GetMapping("/usuarios/{userId}/solicitudes-pendientes")
    public ResponseEntity<List<Map<String, Object>>> getSolicitudesPendientes(
            @PathVariable Long userId
    ) {
        // Obtener solicitudes donde el usuario actual es el destinatario
        List<Contacto> solicitudes = contactoRepository.findByContactoIdAndAceptadoFalse(userId);

        // Mapear a formato JSON
        List<Map<String, Object>> response = solicitudes.stream()
                .map(solicitud -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("solicitudId", solicitud.getId());
                    map.put("usuarioId", solicitud.getUsuario().getId());
                    map.put("nombre", solicitud.getUsuario().getNombre());
                    map.put("username", solicitud.getUsuario().getUsername());
                    map.put("avatar", "http://localhost:8080/uploads/avatars/" + solicitud.getUsuario().getAvatar());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuarios/{userId}/solicitudes-enviadas")
    public ResponseEntity<List<Map<String, Object>>> getSolicitudesEnviadas(
            @PathVariable Long userId
    ) {
        List<Contacto> solicitudes = contactoRepository.findByUsuarioId(userId);

        List<Map<String, Object>> response = solicitudes.stream()
                .map(solicitud -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", solicitud.getId());
                    map.put("name", solicitud.getContacto().getNombre());
                    map.put("username", solicitud.getContacto().getUsername());
                    map.put("avatar", "http://localhost:8080/uploads/avatars/" + solicitud.getContacto().getAvatar());
                    map.put("aceptado", solicitud.isAceptado());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuarios/{id}/contactos")
    public ResponseEntity<List<Usuario>> getContactos(@PathVariable Long id) {
        // Obtener contactos donde el usuario es el emisor (usuario_id = id)
        List<Contacto> contactosComoEmisor = contactoRepository.findByUsuarioIdAndAceptadoTrue(id);

        // Obtener contactos donde el usuario es el receptor (contacto_id = id)
        List<Contacto> contactosComoReceptor = contactoRepository.findByContactoIdAndAceptadoTrue(id);

        // Combinar y eliminar duplicados
        Set<Usuario> contactosUnicos = new HashSet<>();
        contactosComoEmisor.forEach(c -> contactosUnicos.add(c.getContacto()));
        contactosComoReceptor.forEach(c -> contactosUnicos.add(c.getUsuario()));

        return ResponseEntity.ok(new ArrayList<>(contactosUnicos));
    }

    @PostMapping("/usuarios/{idUsuario}/agregar-contacto/{idContacto}")
    public ResponseEntity<String> enviarSolicitud(
            @PathVariable Long idUsuario,
            @PathVariable Long idContacto
    ) {
        Usuario usuarioOrigen = userRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Usuario usuarioDestino = userRepository.findById(idContacto)
                .orElseThrow(() -> new RuntimeException("Usuario destino no encontrado"));

        // Verifica si ya existe una solicitud (en cualquier dirección)
        if (contactoRepository.existsByUsuarioAndContacto(usuarioOrigen, usuarioDestino)) {
            return ResponseEntity.badRequest().body("Ya existe una solicitud pendiente o ya son contactos");
        }

        Contacto nuevaSolicitud = Contacto.builder()
                .usuario(usuarioOrigen)
                .contacto(usuarioDestino)
                .aceptado(false)
                .build();

        contactoRepository.save(nuevaSolicitud);
        return ResponseEntity.ok("Solicitud enviada correctamente");
    }


    @Transactional
    @PostMapping(value = "/api/v1/auth/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> crearUsuario(@Valid @ModelAttribute UserRegisterDTO registroDTO, WebRequest request) {

        // Validar contraseñas (esto no está cubierto por tu ExceptionHandler)
        if (!registroDTO.getPassword().equals(registroDTO.getPassword2())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));
        }

        // Guardar la imagen
        String nombreArchivo = guardarFotos(registroDTO.getAvatar());

        // Establecer el nombre del archivo en el request (para limpieza en caso de error)
        request.setAttribute("nombreArchivo", nombreArchivo, WebRequest.SCOPE_REQUEST);

        // Intentar guardar el usuario (deja que el ExceptionHandler maneje DataIntegrityViolationException)
        Usuario usuario = Usuario.builder()
                .nombre(registroDTO.getNombre())
                .username(registroDTO.getUsername())
                .password(passwordEncoder.encode(registroDTO.getPassword()))
                .avatar(nombreArchivo)
                .email(registroDTO.getEmail())
                .build();

        userRepository.save(usuario); // Si falla, se lanzará DataIntegrityViolationException

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario creado exitosamente"));
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

    public String guardarFotos(MultipartFile foto) {
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

                return nombreFoto; // Devolver el nombre del archivo

            } catch (IOException e) {
                throw new RuntimeException("Error al guardar y redimensionar la imagen: " + e.getMessage(), e);
            }
        }
        return null; // Si no hay archivo, devolver null
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
