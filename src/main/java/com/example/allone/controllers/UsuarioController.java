package com.example.allone.controllers;

import com.example.allone.DTO.LoginRequestDTO;
import com.example.allone.DTO.LoginResponseDTO;
import com.example.allone.DTO.UserRegisterDTO;
import com.example.allone.config.JwtTokenProvider;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Map<String,String>> crearUsuario(@RequestBody UserRegisterDTO userRegisterDTO) {
        try {
            if(userRegisterDTO.getPassword().equals(userRegisterDTO.getPassword2())){
                Usuario usuario = this.userRepository.save(
                        Usuario.builder()
                                .nombre(userRegisterDTO.getNombre())
                                .username(userRegisterDTO.getUsername())
                                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                                .email(userRegisterDTO.getEmail())
                                .avatar(userRegisterDTO.getAvatar())
                                .build());

                return ResponseEntity.status(HttpStatus.CREATED).body(
                        Map.of("message","Usuario creado exitosamente")
                );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error","Las credenciales no coinciden")
            );
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","Email o username ya utilizado"));
        }
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> crearTokenUsuario(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            //Validamos al usuario en Spring (hacemos login manualmente)
            UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
            Authentication auth = authenticationManager.authenticate(userPassAuthToken);    //valida el usuario y devuelve un objeto Authentication con sus datos
            //Obtenemos el UserEntity del usuario logueado
            Usuario user = (Usuario) auth.getPrincipal();

            //Generamos un token con los datos del usuario (la clase tokenProvider ha hemos creado nosotros para no poner aquí todo el código
            String token = this.tokenProvider.generateToken(auth);

            //Devolvemos un código 200 con el username y token JWT
            return ResponseEntity.ok(new LoginResponseDTO(user.getId(), user.getUsername(), token));
        }catch (Exception e) {  //Si el usuario no es válido, salta una excepción BadCredentialsException
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                            "path", "/api/v1/auth/login",
                            "message", "Credenciales erróneas",
                            "timestamp", new Date()
                    )
            );
        }
    }
}
