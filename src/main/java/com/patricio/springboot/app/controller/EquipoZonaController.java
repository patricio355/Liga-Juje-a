package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.dto.EquipoZonaDTO;
import com.patricio.springboot.app.service.EquipoZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos-zona")
public class EquipoZonaController {

    private EquipoZonaService equipoZonaService;

    public EquipoZonaController(EquipoZonaService equipoZonaService) {
        this.equipoZonaService = equipoZonaService;
    }

    // 1. Inscribir un equipo a una zona
    @PostMapping("/inscribir/{equipoId}/zona/{zonaId}")
    public ResponseEntity<EquipoZonaDTO> inscribir(
            @PathVariable Long equipoId,
            @PathVariable Long zonaId
    ) {
        EquipoZonaDTO dto = equipoZonaService.inscribirEquipo(equipoId, zonaId);
        return ResponseEntity.ok(dto);
    }

    // 2. Listar equipos de una zona
    @GetMapping("/zona/{zonaId}/equipos")
    public ResponseEntity<List<EquipoDTO>> listarPorZona(@PathVariable Long zonaId) {
        return ResponseEntity.ok(equipoZonaService.listarEquiposPorZona(zonaId));
    }

    // 3. Eliminar (dar de baja) a un equipo de una zona
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        equipoZonaService.eliminarParticipacion(id);
        return ResponseEntity.noContent().build();
    }
    // 4. Obtener estadísticas de un equipo en una zona
    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<EquipoZonaDTO> estadisticas(@PathVariable Long id) {
        return ResponseEntity.ok(equipoZonaService.obtenerEstadisticas(id));
    }
}

