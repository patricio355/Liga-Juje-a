package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.PartidoProgramadoDTO;
import com.patricio.springboot.app.dto.TarjetaProgramacionEquipoDTO;
import com.patricio.springboot.app.service.ProgramacionFechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programacion")
@RequiredArgsConstructor
public class ProgramacionFechaController {

    private final ProgramacionFechaService service;

    @PreAuthorize("hasAnyRole('ADMIN','ENCARGADOTORNEO')")
    @GetMapping("/zona/{zonaId}/fecha/{fecha}/opciones")
    public List<TarjetaProgramacionEquipoDTO> opciones(
            @PathVariable Long zonaId,
            @PathVariable Integer fecha
    ) {
        return service.obtenerOpciones(zonaId, fecha);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ENCARGADOTORNEO')")
    @PostMapping("/zona/{zonaId}/fecha/{fecha}/partido/{partidoId}")
    public ResponseEntity<?> programar(
            @PathVariable Long zonaId,
            @PathVariable Integer fecha,
            @PathVariable Long partidoId
    ) {
        service.programarPartido(zonaId, fecha, partidoId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/zona/{zonaId}/fecha/{fecha}")
    public List<PartidoProgramadoDTO> programacion(
            @PathVariable Long zonaId,
            @PathVariable Integer fecha
    ) {
        return service.obtenerProgramacion(zonaId, fecha);
    }

    @GetMapping("/zona/{zonaId}/fechas-disponibles")
    public List<Integer> getFechas(@PathVariable Long zonaId) {
        return service.obtenerFechasDisponibles(zonaId);
    }
}
