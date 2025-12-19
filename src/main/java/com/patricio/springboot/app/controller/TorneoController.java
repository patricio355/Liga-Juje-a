package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.EquipoZonaDTO;
import com.patricio.springboot.app.dto.TorneoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.service.EquipoService;
import com.patricio.springboot.app.service.EquipoZonaService;
import com.patricio.springboot.app.service.TorneoService;
import com.patricio.springboot.app.service.ZonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/torneos")
@Tag(name = "Torneos", description = "Operaciones relacionadas con torneos")
public class TorneoController {

    private final TorneoService torneoService;
    private final ZonaService zonaService;
    private final EquipoZonaService equipoZonaService;


    // ---------------------------------------------------------
    // CREAR TORNEO
    // ---------------------------------------------------------
    @PostMapping
    public ResponseEntity<TorneoDTO> crear(@RequestBody TorneoDTO dto) {
        return ResponseEntity.ok(torneoService.crearTorneo(dto));
    }

    // ---------------------------------------------------------
    // LISTAR TODOS LOS TORNEOS
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<TorneoDTO>> listar() {
        return ResponseEntity.ok(torneoService.listarTorneos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<TorneoDTO>> listarActivos() {
        return ResponseEntity.ok(torneoService.listarActivos());
    }



    @GetMapping("/disponibles/equipo/{equipoId}")
    public List<TorneoDTO> torneosDisponibles(@PathVariable Long equipoId) {
        return torneoService.torneosDisponiblesParaEquipo(equipoId);
    }

    // ---------------------------------------------------------
    // MODIFICAR TORNEO
    // ---------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<TorneoDTO> modificar(
            @PathVariable Long id,
            @RequestBody TorneoDTO dto
    ) {
        return ResponseEntity.ok(torneoService.modificarTorneo(id, dto));
    }

    // ---------------------------------------------------------
    // ELIMINAR (SOFT DELETE = cambiar estado)
    // ---------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        torneoService.eliminarTorneo(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // AGREGAR ZONA A UN TORNEO
    // ---------------------------------------------------------
    @PostMapping("/{idTorneo}/zonas")
    public ResponseEntity<TorneoDTO> agregarZona(
            @PathVariable Long idTorneo,
            @RequestBody ZonaDTO dto
    ) {
        return ResponseEntity.ok(torneoService.agregarZona(idTorneo, dto));
    }

    // ---------------------------------------------------------
    // QUITAR ZONA DE UN TORNEO
    // ---------------------------------------------------------
    @DeleteMapping("/{idTorneo}/zonas/{idZona}")
    public ResponseEntity<TorneoDTO> quitarZona(
            @PathVariable Long idTorneo,
            @PathVariable Long idZona
    ) {
        return ResponseEntity.ok(torneoService.quitarZona(idTorneo, idZona));
    }

    @GetMapping("/zonas/torneo/{torneoId}")
    public List<ZonaDTO> listarZonas(@PathVariable Long torneoId) {
        return zonaService.listarPorTorneo(torneoId);
    }

    // 1. Inscribir un equipo a una zona
    @PostMapping("/inscribir/{equipoId}/zona/{zonaId}")
    public ResponseEntity<EquipoZonaDTO> inscribir(
            @PathVariable Long equipoId,
            @PathVariable Long zonaId
    ) {
        EquipoZonaDTO dto = torneoService.agregarEquipoAZona(equipoId, zonaId);
        return ResponseEntity.ok(dto);
    }
}
