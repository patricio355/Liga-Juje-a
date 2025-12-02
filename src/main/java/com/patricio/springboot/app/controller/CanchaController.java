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

    private CanchaService canchaService;

    public CanchaController (CanchaService canchaService){
        this.canchaService = canchaService;
    }

    //listar todas las canchas
    @GetMapping
    @Operation(summary = "Listar todas las canchas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Cancha.class))))
            })
    public ResponseEntity<List<CanchaDTO>> listar() {
        log.info("Listando canchas...");
        List<CanchaDTO> lista = canchaService.listarCanchas() ;
        return ResponseEntity.ok(lista);
    }



    // obtener cancha por id
    @GetMapping("/{id}")
    @Operation()
    public ResponseEntity<CanchaDTO> obtenerCanchaPorID( @PathVariable Long id) {
        log.info("Obteniendo cancha por ID...");
        Optional<CanchaDTO> cancha = canchaService.buscarForID(id);
        return cancha.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation()
    public ResponseEntity<CanchaDTO> crearCancha(@RequestBody CanchaDTO cancha) {
        log.info("Creando cancha...");
        CanchaDTO creado = canchaService.crearCancha(cancha);
        return ResponseEntity.ok().body(creado);
    }
}
