package com.patricio.springboot.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173") // Agregamos 127.0.0.1 por si acaso
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        // ✅ ESTO ES CLAVE: Cachea el permiso por 1 hora (3600 segundos)
                        // Evita que el navegador lance el 'Preflight' de 13s en cada recarga
                        .maxAge(3600);
            }
        };
    }
}