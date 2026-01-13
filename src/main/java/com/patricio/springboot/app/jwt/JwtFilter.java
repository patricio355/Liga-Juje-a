package com.patricio.springboot.app.jwt;

import com.patricio.springboot.app.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        try {
            // 1. Extraer el token del header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                email = jwtUtil.extractUsername(token);
            }

            // 2. Si hay email y no hay autenticación previa en el contexto
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(email);

                // 3. Validar el token contra los datos del usuario
                if (jwtUtil.validateToken(token, user)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establecer la autenticación en Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // Manejo específico si el token caducó mientras el usuario navegaba
            logger.warn("El token JWT ha expirado");
        } catch (Exception e) {
            // Cualquier otro error de parseo o firma del token
            logger.error("Error procesando el token JWT: " + e.getMessage());
        }

        // 4. Continuar con la cadena de filtros pase lo que pase
        filterChain.doFilter(request, response);
    }
}