package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.LoginDTO;
import com.patricio.springboot.app.dto.TokenDTO;
import com.patricio.springboot.app.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginDTO dto) {

        // Autenticar usuario
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        // Obtener el principal como UserDetails
        UserDetails user = (UserDetails) auth.getPrincipal();

        // GENERAR TOKEN CORRECTAMENTE
        String token = jwtUtil.generateToken(user);

        return new TokenDTO(token);
    }

    @GetMapping("/encode")
    public String encode() {
        return new BCryptPasswordEncoder().encode("123");
    }
}
