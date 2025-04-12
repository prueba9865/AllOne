package com.example.allone.controllers;

import com.example.allone.config.JwtTokenProvider;
import com.example.allone.models.UsuarioGoogle;
import com.example.allone.repositories.UsuarioGoogleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.SecretKey;
import java.util.Map;

@Controller
public class UsuarioGoogleController {
    @Autowired
    private UsuarioGoogleRepository userGoogleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/decode-jwt")
    public ResponseEntity<Map<String, ?>> getAvatar(@RequestHeader("Authorization") String authHeader){
        // 1. Extraer el token del header (eliminar "Bearer ")
        String jwtToken = authHeader.replace("Bearer ", "");

        SecretKey key = jwtTokenProvider.claveFirma();

        // 3. Decodificar y verificar el token
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();

        if (claims.get("id") != null){
            return ResponseEntity.ok().body(Map.of("avatar", "http://localhost:8080/uploads/avatars/" + claims.get("avatar", String.class), "id", claims.get("id")));
        }
        return ResponseEntity.ok().body(Map.of("avatar", claims.get("avatar", String.class)));
    }

    @GetMapping("/add")
    @ResponseBody
    public ResponseEntity<?> home(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            System.out.println("Error: usuario no autenticado");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5500/error.html")
                    .build();
        }

        if(this.userGoogleRepository.findByUsername(user.getAttribute("given_name")).isPresent()){
            // Redirigir a una IP y puerto específicos
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5500/home.html")
                    .build();
        }

        // Guardar el usuario en la base de datos
        UsuarioGoogle usuarioGoogle = UsuarioGoogle.builder()
                .email(user.getAttribute("email"))
                .nombre(user.getAttribute("name"))
                .avatar(user.getAttribute("picture"))
                .username(user.getAttribute("given_name"))
                .build();
        this.userGoogleRepository.save(usuarioGoogle);

        System.out.println("Usuario autenticado: " + user.getAttributes());

        // Redirigir a una IP y puerto específicos
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "http://localhost:5500/home.html")
                .build();
    }
}
