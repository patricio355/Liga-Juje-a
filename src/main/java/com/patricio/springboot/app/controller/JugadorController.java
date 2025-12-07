package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.JugadorDTO;
import com.patricio.springboot.app.entity.Jugador;
import com.patricio.springboot.app.repository.JugadorRepository;
import com.patricio.springboot.app.service.JugadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
