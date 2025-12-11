package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.service.ZonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/zonas")
@Tag(name = "Zonas", description = "Operaciones relacionadas con las zonas")
public class ZonaController {

    private final ZonaService zonaService;

    @PutMapping("/{id}")
    public ResponseEntity<ZonaDTO> editarZona(
            @PathVariable Long id,
            @RequestBody ZonaDTO dto
    ) {
        return ResponseEntity.ok(zonaService.editarZona(id, dto));
    }
}
