package com.example.allone.controllers;

import com.example.allone.models.UsuarioGoogle;
import com.example.allone.repositories.UsuarioGoogleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UsuarioGoogleController {
    @Autowired
    private UsuarioGoogleRepository userGoogleRepository;

    @GetMapping("/home")
    @ResponseBody
    public String home(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            System.out.println("Error: usuario no autenticado");
            return "Usuario no autenticado";
        }
        UsuarioGoogle usuarioGoogle = UsuarioGoogle.builder()
                        .email(user.getAttribute("email"))
                        .nombre(user.getAttribute("name"))
                        .avatar(user.getAttribute("picture")).build();
        this.userGoogleRepository.save(usuarioGoogle);
        System.out.println("Usuario autenticado: " + user.getAttributes());
        return "Bienvenido, " + user.getAttribute("name") + " (" + user.getAttribute("email") + ")";
    }
}
