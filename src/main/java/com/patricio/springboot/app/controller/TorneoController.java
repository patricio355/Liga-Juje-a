package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.EquipoZonaDTO;
import com.patricio.springboot.app.dto.EtapaFaseFinalDTO;
import com.patricio.springboot.app.dto.TorneoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.EtapaTorneo;
import com.patricio.springboot.app.repository.EtapaTorneoRepository;
import com.patricio.springboot.app.service.EquipoService;
import com.patricio.springboot.app.service.EquipoZonaService;
import com.patricio.springboot.app.service.TorneoService;
import com.patricio.springboot.app.service.ZonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/torneos")
@Tag(name = "Torneos", description = "Operaciones relacionadas con torneos")
public class TorneoController {

    private final TorneoService torneoService;
    private final ZonaService zonaService;
    private final EquipoZonaService equipoZonaService;
    private final EtapaTorneoRepository etapaTorneoRepository;


    // ---------------------------------------------------------
    // CREAR TORNEO
    // ---------------------------------------------------------
    @PreAuthorize("hasAnyRole('ADMIN','ENCARGADOTORNEO')")
    @PostMapping
    public ResponseEntity<TorneoDTO> crear(
            @RequestBody TorneoDTO dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                torneoService.crearTorneo(dto, auth)
        );
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


    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADOTORNEO')")
    @GetMapping("/disponibles/equipo/{equipoId}")
    public List<TorneoDTO> torneosDisponibles(@PathVariable Long equipoId) {
        return torneoService.torneosDisponiblesParaEquipo(equipoId);
    }

    // ---------------------------------------------------------
    // MODIFICAR TORNEO
    // ---------------------------------------------------------
    @PreAuthorize("hasAnyRole('ADMIN','ENCARGADOTORNEO')")
    @PutMapping("/{id}")
    public ResponseEntity<TorneoDTO> modificar(
            @PathVariable Long id,
            @RequestBody TorneoDTO dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                torneoService.modificarTorneo(id, dto, auth)
        );
    }
    // ---------------------------------------------------------
    // ELIMINAR (SOFT DELETE = cambiar estado)
    // ---------------------------------------------------------
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADOTORNEO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        torneoService.eliminarTorneo(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // AGREGAR ZONA A UN TORNEO
    // ---------------------------------------------------------
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADOTORNEO')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADOTORNEO')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADOTORNEO')")
    @PostMapping("/inscribir/{equipoId}/zona/{zonaId}")
    public ResponseEntity<EquipoZonaDTO> inscribir(
            @PathVariable Long equipoId,
            @PathVariable Long zonaId
    ) {
        EquipoZonaDTO dto = torneoService.agregarEquipoAZona(equipoId, zonaId);
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/mis-torneos")
    @PreAuthorize("hasRole('ENCARGADOTORNEO')")
    public List<TorneoDTO> misTorneos(Authentication authentication) {
        String email = authentication.getName();
        return torneoService.listarTorneosDelEncargado(email);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','ENCARGADOTORNEO')")
    public ResponseEntity<List<TorneoDTO>> dashboard(Authentication auth) {

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (esAdmin) {
            return ResponseEntity.ok(torneoService.listarTorneos());
        }

        return ResponseEntity.ok(
                torneoService.listarTorneosDelEncargado(auth.getName())
        );
    }

    // En TorneoController.java

    @GetMapping("/id/{id}")
    public ResponseEntity<TorneoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerPorId(id));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<TorneoDTO> obtenerPorSlug(@PathVariable String slug) {
        // El service ahora busca por el texto del slug, no por ID
        return ResponseEntity.ok(torneoService.obtenerPorSlug(slug));
    }


    /**
     * Crea o vincula una etapa al torneo (Final, Semis, etc.)
     * Se usa cuando el admin agrega fases en el cuadro de Brackets.
     */
    @PostMapping("/{id}/etapas")
    @PreAuthorize("hasAnyRole('ADMIN','ENCARGADOTORNEO')")
    public ResponseEntity<?> gestionarEtapa(@PathVariable Long id, @RequestBody EtapaTorneo etapaDTO) {
        try {
            EtapaTorneo etapa = torneoService.crearEtapa(
                    id,
                    etapaDTO.getNombre(),
                    etapaDTO.getTipo(),
                    etapaDTO.getOrden()
            );
            return ResponseEntity.ok(etapa);
        } catch (IllegalArgumentException e) {
            // Error de validación de datos (400)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            // Error de lógica o no encontrado (400 o 404)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Error genérico (500)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error inesperado"));
        }
    }

    /**
     * Obtiene todas las etapas configuradas para un torneo.
     * Sirve para que el Front reconstruya el cuadro al cargar la página.
     */
    @GetMapping("/{id}/etapas")
    public ResponseEntity<List<EtapaTorneo>> listarEtapas(@PathVariable Long id) {
        // Buscamos por torneo y ordenamos por 'orden' para que el front reciba la jerarquía correcta
        List<EtapaTorneo> etapas = etapaTorneoRepository.findByTorneoIdOrderByOrdenAsc(id);
        return ResponseEntity.ok(etapas);
    }

    @GetMapping("/{id}/cuadro-fase-final")
    public ResponseEntity<List<EtapaTorneo>> obtenerCuadro(@PathVariable Long id) {
        try {
            List<EtapaTorneo> cuadro = torneoService.obtenerCuadroFaseFinal(id);
            return ResponseEntity.ok(cuadro);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/cuadro-completo")
    public ResponseEntity<List<EtapaFaseFinalDTO>> getCuadroCompleto(@PathVariable Long id) {
        List<EtapaFaseFinalDTO> cuadro = torneoService.obtenerEstructuraCuadro(id);
        return ResponseEntity.ok(cuadro);
    }
}
