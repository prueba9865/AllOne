package com.example.allone.controllers;

import com.example.allone.DTO.LoginRequestDTO;
import com.example.allone.DTO.LoginResponseDTO;
import com.example.allone.DTO.UserRegisterDTO;
import com.example.allone.config.JwtTokenProvider;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.UsuarioRepository;
import jakarta.validation.Valid;
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

import java.util.*;

@RestController
public class UsuarioController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<Map<String, String>> crearUsuario(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getPassword2())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", "Las contraseñas no coinciden")
            );
        }

        Usuario usuario = this.userRepository.save(
                Usuario.builder()
                        .nombre(userRegisterDTO.getNombre())
                        .username(userRegisterDTO.getUsername())
                        .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                        .email(userRegisterDTO.getEmail())
                        .telefono(userRegisterDTO.getTelefono())
                        .avatar(userRegisterDTO.getAvatar())
                        .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "Usuario creado exitosamente")
        );
    }


    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> crearTokenUsuario(@RequestBody LoginRequestDTO loginRequestDTO) {
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

    @GetMapping("/home")
    @ResponseBody
    public String home(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            System.out.println("Error: usuario no autenticado");
            return "Usuario no autenticado";
        }

        System.out.println("Usuario autenticado: " + user.getAttributes());
        return "Bienvenido, " + user.getAttribute("name") + " (" + user.getAttribute("email") + ")";
    }

}
