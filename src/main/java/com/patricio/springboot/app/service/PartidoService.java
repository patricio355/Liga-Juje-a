package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.PartidoCreateDTO;
import com.patricio.springboot.app.entity.EquipoZona;
import com.patricio.springboot.app.entity.EstadisticaJugador;
import com.patricio.springboot.app.entity.Partido;
import com.patricio.springboot.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartidoService {

    private final EquipoZonaRepository equipoZonaRepository;
    private final PartidoRepository partidoRepository;
    private final EquipoZonaService equipoZonaService;
    private final EquipoRepository equipoRepository;
    private final CanchaRepository canchaRepository;
    private final ZonaRepository zonaRepository;




    public Partido crearPartido(PartidoCreateDTO dto) {


        // VALIDAR EQUIPOS EN LA ZONA

        if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoLocalId(), dto.getZonaId()) == null) {
            throw new RuntimeException("El equipo local NO está inscripto en la zona.");
        }

        if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoVisitanteId(), dto.getZonaId()) == null) {
            throw new RuntimeException("El equipo visitante NO está inscripto en la zona.");
        }

        Partido partido = new Partido();

        partido.setEquipoLocal(
                equipoRepository.findById(dto.getEquipoLocalId())
                        .orElseThrow(() -> new RuntimeException("Equipo local no encontrado"))
        );

        partido.setEquipoVisitante(
                equipoRepository.findById(dto.getEquipoVisitanteId())
                        .orElseThrow(() -> new RuntimeException("Equipo visitante no encontrado"))
        );

        partido.setCancha(
                canchaRepository.findById(dto.getCanchaId())
                        .orElseThrow(() -> new RuntimeException("Cancha no encontrada"))
        );

        partido.setZona(
                zonaRepository.findById(dto.getZonaId())
                        .orElseThrow(() -> new RuntimeException("Zona no encontrada"))
        );

//        partido.setEtapa(
//                etapaRepository.findById(dto.getEtapaId())
//                        .orElseThrow(() -> new RuntimeException("Etapa no encontrada"))
//        );
//
//        partido.setArbitro(
//                arbitroRepository.findById(dto.getArbitroId())
//                        .orElseThrow(() -> new RuntimeException("Árbitro no encontrado"))
//        );

        partido.setFecha(LocalDate.parse(dto.getFecha()));
        partido.setVeedor(dto.getVeedor());
        partido.setEstado("PENDIENTE");

        return partidoRepository.save(partido);
    }


    // CERRAR PARTIDO CALCULANDO GOLES AUTOMÁTICAMENTE

    public Partido cerrarPartido(Long partidoId) {

        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        if ("FINALIZADO".equalsIgnoreCase(partido.getEstado())) {
            throw new RuntimeException("El partido ya está finalizado.");
        }

        List<EstadisticaJugador> stats = partido.getEstadisticas();

        int golesLocal = stats.stream()
                .filter(e -> e.getJugador().getEquipo().getId().equals(partido.getEquipoLocal().getId()))
                .mapToInt(EstadisticaJugador::getGoles)
                .sum();

        int golesVisitante = stats.stream()
                .filter(e -> e.getJugador().getEquipo().getId().equals(partido.getEquipoVisitante().getId()))
                .mapToInt(EstadisticaJugador::getGoles)
                .sum();

        actualizarStatsPorResultado(partido, golesLocal, golesVisitante);

        partido.setEstado("FINALIZADO");
        if (golesLocal < golesVisitante) {
            partido.setGanador(partido.getEquipoVisitante());
        }

        if (golesLocal > golesVisitante){
            partido.setGanador(partido.getEquipoLocal());
        }


        return partidoRepository.save(partido);
    }


    // ACTUALIZAR ESTADÍSTICAS DEL TORNEO

    private void actualizarStatsPorResultado(Partido partido, int golesLocal, int golesVisitante) {

        EquipoZona localZona = equipoZonaRepository.findByEquipoIdAndZonaId(
                partido.getEquipoLocal().getId(), partido.getZona().getId()
        );

        EquipoZona visitanteZona = equipoZonaRepository.findByEquipoIdAndZonaId(
                partido.getEquipoVisitante().getId(), partido.getZona().getId()
        );

        if (localZona == null || visitanteZona == null) {
            throw new RuntimeException("Uno de los equipos no está inscripto en la zona.");
        }

        // Sumar estadísticas
        actualizarStats(localZona, golesLocal, golesVisitante);
        actualizarStats(visitanteZona, golesVisitante, golesLocal);

        equipoZonaRepository.save(localZona);
        equipoZonaRepository.save(visitanteZona);
    }


    // LÓGICA FUTBOLÍSTICA DE SUMA DE PUNTOS

    private void actualizarStats(EquipoZona ez, int golesHechos, int golesRecibidos) {

        ez.setPartidosJugados(ez.getPartidosJugados() + 1);
        ez.setGolesAFavor(ez.getGolesAFavor() + golesHechos);
        ez.setGolesEnContra(ez.getGolesEnContra() + golesRecibidos);

        if (golesHechos > golesRecibidos) {
            ez.setGanados(ez.getGanados() + 1);
            ez.setPuntos(ez.getPuntos() + 3);
        } else if (golesHechos == golesRecibidos) {
            ez.setEmpatados(ez.getEmpatados() + 1);
            ez.setPuntos(ez.getPuntos() + 1);
        } else {
            ez.setPerdidos(ez.getPerdidos() + 1);
        }
    }
}
