package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.FixtureFechaDTO;
import com.patricio.springboot.app.dto.PartidoCreateDTO;
import com.patricio.springboot.app.dto.ResultadoPartidoResponse;
import com.patricio.springboot.app.entity.Partido;
import com.patricio.springboot.app.mapper.PartidoMapper;
import com.patricio.springboot.app.service.PartidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    // 🔹 FIXTURE POR ZONA
    @GetMapping("/zona/{idZona}/fixture")
    public List<FixtureFechaDTO> obtenerFixturePorZona(@PathVariable Long idZona) {
        return partidoService.obtenerFixturePorZona(idZona);
    }


    /**
     * 🔹 Generar fixture inicial (round-robin limpio)
     * SOLO se puede ejecutar una vez por zona
     */
    @PostMapping("/zona/{zonaId}/fixture")
    public ResponseEntity<?> generarFixtureInicial(
            @PathVariable Long zonaId
    ) {
        partidoService.generarFixtureInicialZona(zonaId);
        return ResponseEntity.ok("Fixture generado correctamente");
    }



    //regenerar
    @PostMapping("/zona/{zonaId}/fixture/regenerar")
    public ResponseEntity<?> regenerarFixture(@PathVariable Long zonaId) {
        partidoService.regenerarFixtureZona(zonaId);
        return ResponseEntity.ok("Fixture regenerado correctamente");
    }
}
