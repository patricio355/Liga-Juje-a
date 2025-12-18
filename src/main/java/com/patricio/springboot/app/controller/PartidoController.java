package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.*;
import com.patricio.springboot.app.entity.Partido;
import com.patricio.springboot.app.entity.Usuario;
import com.patricio.springboot.app.mapper.PartidoMapper;
import com.patricio.springboot.app.repository.SolicitudCierrePartidoRepository;
import com.patricio.springboot.app.repository.UsuarioRepository;
import com.patricio.springboot.app.service.PartidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final PartidoService partidoService;
    private final SolicitudCierrePartidoRepository solicitudRepo;
    private final UsuarioRepository usuarioRepository;
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PartidoCreateDTO dto) {

        Partido partido = partidoService.crearPartido(dto);

        return ResponseEntity.ok(PartidoMapper.toDTO(partido));
    }

    // =============================================
    // CERRAR PARTIDO DESDE PANEL DEL ÁRBITRO
    // =============================================
//    @PostMapping("/{id}/cerrar")
//    public ResponseEntity<ResultadoPartidoResponse> cerrarPartido(@PathVariable Long id) {
//        try {
//            Partido partido = partidoService.cerrarPartido(id);
//
//            ResultadoPartidoResponse response = new ResultadoPartidoResponse(partido);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            e.printStackTrace(); // MUESTRA EL ERROR REAL
//            return ResponseEntity.badRequest().body(null);
//        }
//    }


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

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cerrar")
    public ResponseEntity<?> cerrarPartidoAdmin(
            @PathVariable Long id,
            @RequestBody SolicitudCierreRequest request
    ) {
        partidoService.cerrarPartidoDirecto(
                id,
                request.getGolesLocal(),
                request.getGolesVisitante()
        );

        return ResponseEntity.ok(
                Map.of("message", "Partido cerrado")
        );
    }


    @PostMapping("/{id}/solicitar-cierre")
    public ResponseEntity<?> solicitarCierre(
            @PathVariable Long id,
            @RequestBody SolicitudCierreRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        partidoService.solicitarCierre(
                id,
                usuario,
                request.getGolesLocal(),
                request.getGolesVisitante()
        );

        return ResponseEntity.ok("Solicitud enviada");
    }

    @GetMapping("/solicitudes-cierre")
    public List<SolicitudCierreResponse> listarSolicitudes() {
        return solicitudRepo.findByEstado("PENDIENTE")
                .stream()
                .map(s -> new SolicitudCierreResponse(
                        s.getId(),
                        s.getPartido().getId(),
                        s.getPartido().getEquipoLocal().getNombre(),
                        s.getPartido().getEquipoVisitante().getNombre(),
                        s.getGolesLocal(),
                        s.getGolesVisitante(),
                        s.getSolicitante().getNombre(),
                        s.getEstado()
                ))
                .toList();
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id) {
        partidoService.aprobarSolicitud(id);
        return ResponseEntity.ok("Solicitud aprobada");
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Long id) {
        partidoService.rechazarSolicitud(id);
        return ResponseEntity.ok("Solicitud rechazada");
    }
}
