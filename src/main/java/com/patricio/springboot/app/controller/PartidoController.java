package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.PartidoCreateDTO;
import com.patricio.springboot.app.dto.ResultadoPartidoResponse;
import com.patricio.springboot.app.entity.Partido;
import com.patricio.springboot.app.mapper.PartidoMapper;
import com.patricio.springboot.app.service.PartidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final PartidoService partidoService;


    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PartidoCreateDTO dto) {

        Partido partido = partidoService.crearPartido(dto);

        return ResponseEntity.ok(PartidoMapper.toDTO(partido));
    }

    // =============================================
    // CERRAR PARTIDO DESDE PANEL DEL ÁRBITRO
    // =============================================
    @PostMapping("/{id}/cerrar")
    public ResponseEntity<ResultadoPartidoResponse> cerrarPartido(@PathVariable Long id) {
        try {
            Partido partido = partidoService.cerrarPartido(id);

            ResultadoPartidoResponse response = new ResultadoPartidoResponse(partido);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // MUESTRA EL ERROR REAL
            return ResponseEntity.badRequest().body(null);
        }
    }



}
