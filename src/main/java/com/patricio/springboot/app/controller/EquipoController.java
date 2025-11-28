package com.patricio.springboot.app.controller;


import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.service.EquipoService;
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
    @GetMapping
    @Operation(summary = "Listar todos los equipos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Equipo.class))))
            })
    public ResponseEntity<List<EquipoDTO>> listar() {
        log.info("Listando envios...");
        List<EquipoDTO> lista = equipoService.listarEquipos() ;
        return ResponseEntity.ok(lista);
    }
}
