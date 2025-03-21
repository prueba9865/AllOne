package com.example.allone.controllers;

import com.example.allone.models.UsuarioGoogle;
import com.example.allone.repositories.UsuarioGoogleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class UsuarioGoogleController {
    @Autowired
    private UsuarioGoogleRepository userGoogleRepository;

    @GetMapping("/add")
    @ResponseBody
    public ResponseEntity<?> home(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            System.out.println("Error: usuario no autenticado");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5500/error.html")
                    .build();
        }

        // Guardar el usuario en la base de datos
        UsuarioGoogle usuarioGoogle = UsuarioGoogle.builder()
                .email(user.getAttribute("email"))
                .nombre(user.getAttribute("name"))
                .avatar(user.getAttribute("picture"))
                .build();
        this.userGoogleRepository.save(usuarioGoogle);

        System.out.println("Usuario autenticado: " + user.getAttributes());

        // Redirigir a una IP y puerto específicos
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "http://localhost:5500/home.html")
                .build();
    }
}
