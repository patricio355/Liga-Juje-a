package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.JugadorDTO;
import com.patricio.springboot.app.entity.Jugador;
import com.patricio.springboot.app.repository.JugadorRepository;
import com.patricio.springboot.app.service.JugadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Jugadores", description = "Operaciones relacionadas con los jugadores")
@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {
   private JugadorService jugadorService;
   public JugadorController(JugadorService jugadorService) {
       this.jugadorService = jugadorService;
   }

    @GetMapping("/{id}")
    @Operation()
    public ResponseEntity<Jugador> obtenerJugador(@PathVariable Long id) {
        log.info("Obteniendo jugador por ID {}", id);
        Jugador jugador = jugadorService.obtenerJugadorID(id);
        return new ResponseEntity<>(jugador, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<JugadorDTO> crearJugador(@RequestBody @Valid JugadorDTO dto) {
        JugadorDTO jugador = jugadorService.crearJugador(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(jugador);
    }

}
