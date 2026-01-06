package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.PartidoProgramadoDTO;
import com.patricio.springboot.app.dto.TarjetaProgramacionEquipoDTO;
import com.patricio.springboot.app.service.PartidoService;
import com.patricio.springboot.app.service.ProgramacionFechaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/programacion")
@RequiredArgsConstructor
public class ProgramacionFechaController {

    private final ProgramacionFechaService service;
    private final PartidoService partidoService;

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

    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADOTORNEO')")
    @PutMapping("/detalles/{partidoId}")
    public ResponseEntity<?> actualizarDetalles(
            @PathVariable Long partidoId,
            @RequestBody Map<String, String> body
    ) {
        try {
            // Extraemos los valores del body
            String fecha = body.get("fecha"); // Formato esperado: "yyyy-MM-dd"
            String hora = body.get("hora");   // Formato esperado: "HH:mm"
            String cancha = body.get("cancha");
            String arbitro = body.get("arbitro");

            // Llamamos al servicio con los nuevos parámetros de fecha y hora
            partidoService.actualizarDetallesProgramacion(
                    partidoId,
                    fecha,
                    hora,
                    cancha,
                    arbitro
            );

            return ResponseEntity.ok(Map.of("message", "Detalles actualizados correctamente"));
        } catch (Exception e) {
            // Retornamos el error para que el frontend pueda mostrarlo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
