package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.EstadisticaJugadorDTO;
import com.patricio.springboot.app.service.EstadisticaJugadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticaJugadorController {

    private final EstadisticaJugadorService estadisticaJugadorService;

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody EstadisticaJugadorDTO dto) {
        try {
            return ResponseEntity.ok(estadisticaJugadorService.registrarEstadistica(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}