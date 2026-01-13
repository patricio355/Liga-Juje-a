package com.patricio.springboot.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo de Usuario Deshabilitado (activo = 0)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabled(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(Map.of(
                        "message", "Tu cuenta está desactivada. Por favor, contacta al administrador.",
                        "error", "ACCOUNT_DISABLED"
                ));
    }

    // 2. Manejo de Credenciales Incorrectas (Password o Email mal)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(Map.of(
                        "message", "Email o contraseña incorrectos.",
                        "error", "BAD_CREDENTIALS"
                ));
    }

    // 3. Manejo de Errores de Runtime (General)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        // Usamos HashMap manual para evitar NullPointerException si getMessage() es nulo
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(body);
    }
}