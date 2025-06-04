package com.example.allone.services;

import com.example.allone.models.ChatIA;
import com.example.allone.models.Usuario;
import com.example.allone.repositories.ChatIARepository;
import com.example.allone.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ChatIARepository chatIARepository;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    private final RestTemplate rt = new RestTemplate();

    public String chat(String userMessage, Long usuarioId) {
        // 1. Obtener el usuario completo desde la base de datos
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Crear y guardar el mensaje en tu base de datos
        ChatIA mensaje = ChatIA.builder()
                .contenido(userMessage)
                .usuario(usuario)
                .build();
        chatIARepository.save(mensaje);

        // 3. Llamar a la API de IA (tu lógica existente)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "deepseek/deepseek-r1:free",
                "messages", List.of(
                        Map.of("role", "system", "content", "Eres un asistente servicial."),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = rt.postForEntity(apiUrl, req, Map.class);

        // Procesar respuesta...
        List<?> choices = (List<?>) resp.getBody().get("choices");
        Map<?,?> first = (Map<?,?>) choices.get(0);
        Map<?,?> message = (Map<?,?>) first.get("message");

        return (String) message.get("content");
    }
}

