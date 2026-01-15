package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.LoginDTO;
import com.patricio.springboot.app.dto.TokenDTO;
import com.patricio.springboot.app.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) { // Cambiamos a ResponseEntity
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );

            UserDetails user = (UserDetails) auth.getPrincipal();

            boolean esAutorizado = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_ENCARGADOTORNEO"));

            if (!esAutorizado) {
                // Enviamos un 403 manual con un mensaje claro
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Acceso restringido a administradores. Tu panel está en desarrollo"));
            }

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new TokenDTO(token));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email o contraseña incorrectos."));
        }
    }

    @GetMapping("/encode")
    public String encode() {
        return new BCryptPasswordEncoder().encode("123");
    }
}
