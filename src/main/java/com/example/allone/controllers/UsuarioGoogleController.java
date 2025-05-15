package com.example.allone.controllers;

import com.example.allone.config.JwtTokenProvider;
import com.example.allone.models.UsuarioGoogle;
import com.example.allone.repositories.UsuarioGoogleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
import java.util.HashMap;
import java.util.Map;

@Controller
public class UsuarioGoogleController {
    @Autowired
    private UsuarioGoogleRepository userGoogleRepository;

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
