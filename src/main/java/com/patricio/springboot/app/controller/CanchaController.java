package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.CanchaDTO;
import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.entity.Cancha;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.service.CanchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@Slf4j
@Tag(name = "Canchas", description = "Operaciones relacionadas con las canchas")
@RestController
@RequestMapping("/api/canchas")
public class CanchaController {

    private final CanchaService canchaService;

    public CanchaController (CanchaService canchaService){
        this.canchaService = canchaService;
    }

    @GetMapping
    public ResponseEntity<List<CanchaDTO>> listar() {
        log.info("Listando canchas...");
        return ResponseEntity.ok(canchaService.listarCanchas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CanchaDTO> obtenerCanchaPorID(@PathVariable Long id) {
        return canchaService.buscarForID(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CanchaDTO> crearCancha(@RequestBody CanchaDTO cancha) {
        log.info("Creando nueva cancha: {}", cancha.getNombre());
        return ResponseEntity.ok(canchaService.crearCancha(cancha));
    }

    // --- NUEVOS MÉTODOS ---

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una cancha existente")
    public ResponseEntity<CanchaDTO> actualizarCancha(@PathVariable Long id, @RequestBody CanchaDTO canchaDTO) {
        log.info("Actualizando cancha ID: {}", id);
        return ResponseEntity.ok(canchaService.actualizarCancha(id, canchaDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una cancha")
    public ResponseEntity<Void> eliminarCancha(@PathVariable Long id) {
        log.info("Eliminando cancha ID: {}", id);
        canchaService.eliminarCancha(id);
        return ResponseEntity.noContent().build();
    }
}