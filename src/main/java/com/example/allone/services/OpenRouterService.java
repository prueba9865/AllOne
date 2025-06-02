package com.example.allone.services;

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

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    private final RestTemplate rt = new RestTemplate();

    public String chat(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "deepseek/deepseek-r1:free",
                "messages", List.of(
                        Map.of("role", "system", "content", "Eres un asistente servicial."),
                        Map.of("role", "user",   "content", userMessage)
                )
        );

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = rt.postForEntity(apiUrl, req, Map.class);

        List<?> choices = (List<?>) resp.getBody().get("choices");
        Map<?,?> first = (Map<?,?>) choices.get(0);
        Map<?,?> message = (Map<?,?>) first.get("message");

        return (String) message.get("content");
    }
}

