package com.example.allone.controllers;

import com.example.allone.DTO.*;
import com.example.allone.config.JwtTokenProvider;
import com.example.allone.errors.ResourceNotFoundException;
import com.example.allone.models.Contacto;
import com.example.allone.models.Usuario;
import com.example.allone.models.UsuarioGoogle;
import com.example.allone.repositories.ContactoRepository;
import com.example.allone.repositories.UsuarioGoogleRepository;
import com.example.allone.repositories.UsuarioRepository;
import com.example.allone.services.UsuarioService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

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
    private UsuarioGoogleRepository usuarioGoogleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/api/v1/usuario/edit/{usuarioId}")
    public ResponseEntity<UsuarioDTO> getUsuarioParaEditar(@PathVariable Long usuarioId) {
        UsuarioDTO usuarioDTO = usuarioService.obtenerUsuarioParaEditar(usuarioId);
        return ResponseEntity.ok(usuarioDTO);
    }

    @PutMapping(value = "/api/v1/usuario/edit/{usuarioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Long usuarioId,
            @Valid @ModelAttribute UsuarioEditDTO dto,
            BindingResult result,
            WebRequest request,
            Authentication authentication) { // Añadir Authentication como parámetro

        // 1. Validación de errores
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err ->
                    errores.put(err.getField(), err.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errores);
        }

        // 2. Guardar la imagen
        String nombreArchivo = guardarFotos(dto.getAvatar());
        request.setAttribute("nombreArchivo", nombreArchivo, WebRequest.SCOPE_REQUEST);

        // 3. Actualizar usuario
        Usuario actualizado = usuarioService.actualizarUsuario(usuarioId, dto, nombreArchivo);

        // 4. Generar nuevo token con los datos actualizados
        String nuevoToken = jwtTokenProvider.generateToken(authentication);

        // 5. Construir respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("success", "Usuario editado correctamente");
        response.put("token", nuevoToken); // Incluir el nuevo token en la respuesta
        response.put("avatarUrl", "http://localhost:8080/uploads/avatars/" + actualizado.getAvatar()); // URL completa del avatar

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

        // 2. Obtener usuarios Google y mapear
        List<Map<String, Object>> usuariosGoogleSimplificados = usuarioGoogleRepository.findAll().stream()
                .map(usuario -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", usuario.getId());
                        map.put("name", usuario.getNombre());
                        map.put("username", usuario.getUsername());
                        map.put("avatar", usuario.getAvatar());
                        return map;
                })
                .collect(Collectors.toList());

        // 3. Combinar ambas listas
        usuariosSimplificados.addAll(usuariosGoogleSimplificados);

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
            // 1. Verificar y decodificar el token JWT
            //String jwt = token.replace("Bearer ", "");
            // Aquí necesitas tu lógica para decodificar el JWT
            // Long userId = jwtUtil.getUserIdFromToken(jwt);

            // 2. Buscar la solicitud
            Contacto solicitud = contactoRepository.findById(solicitudId)
                    .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

            // 3. Verificar que el usuario actual es el destinatario
            // if (!solicitud.getContacto().getId().equals(userId)) {
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No autorizado");
            // }

            // 4. Procesar la respuesta
            if (aceptar) {
                solicitud.setAceptado(true);
                contactoRepository.save(solicitud);

                // Opcional: Crear la relación inversa si es bidireccional
                // Contacto relacionInversa = new Contacto();
                // relacionInversa.setUsuario(solicitud.getContacto());
                // relacionInversa.setContacto(solicitud.getUsuario());
                // relacionInversa.setAceptado(true);
                // contactoRepository.save(relacionInversa);

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
        // 1. Obtener solicitudes donde el usuario actual es el destinatario
        List<Contacto> solicitudes = contactoRepository.findByContactoIdAndAceptadoFalse(userId);

        // 2. Mapear a formato JSON
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
        // 1. Obtener contactos donde el usuario es el emisor (usuario_id = id)
        List<Contacto> contactosComoEmisor = contactoRepository.findByUsuarioIdAndAceptadoTrue(id);

        // 2. Obtener contactos donde el usuario es el receptor (contacto_id = id)
        List<Contacto> contactosComoReceptor = contactoRepository.findByContactoIdAndAceptadoTrue(id);

        // 3. Combinar y eliminar duplicados
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

        // 1. Validar contraseñas (esto no está cubierto por tu ExceptionHandler)
        if (!registroDTO.getPassword().equals(registroDTO.getPassword2())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));
        }

        // 2. Guardar la imagen
        String nombreArchivo = guardarFotos(registroDTO.getAvatar());

        // 🔹 Establecer el nombre del archivo en el request (para limpieza en caso de error)
        request.setAttribute("nombreArchivo", nombreArchivo, WebRequest.SCOPE_REQUEST);

        // 3. Intentar guardar el usuario (deja que el ExceptionHandler maneje DataIntegrityViolationException)
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
