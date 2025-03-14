package com.example.allone.errors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Añadimos al mapa todos los errores de validación
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> errorDetails = new HashMap<>();

        if (ex.getValue() != null && ex.getValue().toString().trim().isEmpty()) {
            errorDetails.put("error", "El parámetro no puede estar vacío o contener espacios");
            errorDetails.put("message", "El valor del parámetro debe ser un número entero válido.");
        } else {
            errorDetails.put("error", "El parámetro debe ser un número entero válido.");
            errorDetails.put("message", "El valor proporcionado no se puede convertir a un número.");
        }

        return ResponseEntity.badRequest().body(errorDetails);  // Respuesta con error 400
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, String> errorDetails = new HashMap<>();

        errorDetails.put("error", "Violación de integridad de datos");
        errorDetails.put("message", "El email o el nombre de usuario ya están en uso. Intenta con otro.");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);  // Código 409 (conflict)
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Date());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Credenciales incorrectas");
        response.put("message", "Usuario o contraseña incorrectos");

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Manejo de excepciones generales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Error interno del servidor");
        response.put("details", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}