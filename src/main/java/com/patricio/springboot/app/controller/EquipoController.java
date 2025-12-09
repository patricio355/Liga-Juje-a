package com.patricio.springboot.app.controller;


import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.service.EquipoService;
import jakarta.persistence.Id;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;

@Slf4j
@Tag(name = "Equipos", description = "Operaciones relacionadas con los equipos")
@RestController
@RequestMapping("/api/equipos")
public class EquipoController {


    private EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    //listar todos los equipos
    @GetMapping("/todos")
    @Operation(summary = "Listar todos los equipos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Equipo.class))))
            })
    public ResponseEntity<List<EquipoDTO>> listar() {
        log.info("Listando equipos...");
        List<EquipoDTO> lista = equipoService.listarEquipos() ;
        return ResponseEntity.ok(lista);
    }

    //listar todos los equipos activos
    @GetMapping
    @Operation(summary = "Listar todos los equipos activos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Equipo.class))))
            })
    public ResponseEntity<List<EquipoDTO>> listarActivos() {
        log.info("Listando equipos...");
        List<EquipoDTO> lista = equipoService.listarEquiposActivos() ;
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @Operation()
    public ResponseEntity<EquipoDTO> crearEquipo(@Valid @RequestBody EquipoDTO equipo) {
        log.info("Creando equipo: {}", equipo);
        EquipoDTO entity = equipoService.crearEquipo(equipo);
        return ResponseEntity.ok().body(entity);
    }



    @DeleteMapping("/{id}")
    @Operation()
    public ResponseEntity<EquipoDTO> eliminarEquipo(@PathVariable Long id) {
        log.info("Eliminando equipo con id: {}", id);
        EquipoDTO entity = equipoService.eliminarEquipo(id);
        return ResponseEntity.ok().body(entity);
    }


    @PutMapping("/{id}")
    @Operation()
    public ResponseEntity<EquipoDTO> editarEquipo(@Valid @RequestBody EquipoDTO equipo, @PathVariable Long id) {
        log.info("Editando equipo con id: {}", id);
        EquipoDTO entity = equipoService.editarEquipo(id, equipo);
        return ResponseEntity.ok().body(entity);
    }

    @PutMapping("/{idEquipo}/cancha/{idCancha}")
    @Operation(summary = "Asignar una cancha a un equipo")
    public ResponseEntity<EquipoDTO> asignarCancha(@PathVariable Long idEquipo,
                                                   @PathVariable Long idCancha) {

        log.info("Asignando cancha {} al equipo {}", idCancha, idEquipo);

        EquipoDTO entity = equipoService.asignarCancha(idEquipo, idCancha);

        return ResponseEntity.ok(entity);
    }

    @PutMapping("/{idEquipo}/zona/{idZona}")
    @Operation()
    public ResponseEntity<EquipoDTO> asignarZona(@PathVariable Long idEquipo,
                                                 @PathVariable Long idZona) {

        EquipoDTO actualizado = equipoService.asignarZona(idEquipo, idZona);
        return ResponseEntity.ok(actualizado);
    }


    @PutMapping("/{idEquipo}/jugadores/{idJugador}")
    public ResponseEntity<EquipoDTO> registrarJugador(@PathVariable Long idEquipo,
                                                      @PathVariable Long idJugador) {

        EquipoDTO actualizado = equipoService.registrarJugador(idEquipo, idJugador);

        return ResponseEntity.ok(actualizado);
    }

}
