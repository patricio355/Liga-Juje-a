package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.OpcionPartidoDTO;
import com.patricio.springboot.app.dto.PartidoProgramadoDTO;
import com.patricio.springboot.app.dto.TarjetaProgramacionEquipoDTO;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.repository.PartidoRepository;
import com.patricio.springboot.app.repository.ProgramacionFechaRepository;
import com.patricio.springboot.app.repository.ZonaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramacionFechaService {

    private final ZonaRepository zonaRepository;
    private final PartidoRepository partidoRepository;
    private final ProgramacionFechaRepository programacionRepository;

    public List<TarjetaProgramacionEquipoDTO> obtenerOpciones(
            Long zonaId,
            Integer numeroFecha
    ) {
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow();

        List<Equipo> equipos = zona.getEquiposZona()
                .stream()
                .map(EquipoZona::getEquipo)
                .toList();

        List<Partido> partidosZona =
                partidoRepository.findByZonaId(zonaId);

        // 🔥 TODOS los partidos ya programados en la zona (cualquier fecha)
        Set<Long> partidosYaProgramados =
                programacionRepository.findByZonaId(zonaId)
                        .stream()
                        .map(pf -> pf.getPartido().getId())
                        .collect(Collectors.toSet());

        // 🔥 partidos ya finalizados
        Set<Long> partidosFinalizados = partidosZona.stream()
                .filter(p -> "FINALIZADO".equals(p.getEstado()))
                .map(Partido::getId)
                .collect(Collectors.toSet());

        List<TarjetaProgramacionEquipoDTO> tarjetas = new ArrayList<>();

        for (Equipo equipo : equipos) {

            TarjetaProgramacionEquipoDTO dto =
                    new TarjetaProgramacionEquipoDTO();

            dto.setEquipoId(equipo.getId());
            dto.setEquipoNombre(equipo.getNombre());
            dto.setSeleccionado(false);
            dto.setBloqueado(false);

            List<OpcionPartidoDTO> opciones = partidosZona.stream()

                    // participa el equipo
                    .filter(p ->
                            p.getEquipoLocal().getId().equals(equipo.getId())
                                    || p.getEquipoVisitante().getId().equals(equipo.getId())
                    )

                    // ❌ nunca mostrar partidos ya usados en el torneo
                    .filter(p -> !partidosYaProgramados.contains(p.getId()))

                    // ❌ nunca mostrar partidos ya jugados
                    .filter(p -> !partidosFinalizados.contains(p.getId()))

                    .map(p -> {
                        OpcionPartidoDTO o = new OpcionPartidoDTO();
                        o.setPartidoId(p.getId());

                        o.setVs(
                                p.getEquipoLocal().getId().equals(equipo.getId())
                                        ? p.getEquipoVisitante().getNombre()
                                        : p.getEquipoLocal().getNombre()
                        );

                        o.setJugado(false);
                        return o;
                    })
                    .toList();

            dto.setOpciones(opciones);
            dto.setBloqueado(opciones.isEmpty());

            tarjetas.add(dto);
        }

        return tarjetas;
    }


    // ✅ 2️⃣ Confirmar un partido
    public void programarPartido(Long zonaId, Integer fecha, Long partidoId) {

        if (programacionRepository.existePartidoEnZona(zonaId, partidoId)) {
            throw new RuntimeException("Este partido ya fue programado en el torneo");
        }

        ProgramacionFecha pf = new ProgramacionFecha();
        pf.setZona(zonaRepository.getReferenceById(zonaId));
        pf.setNumeroFecha(fecha);
        pf.setPartido(partidoRepository.getReferenceById(partidoId));
        pf.setEstado("PROGRAMADO");

        programacionRepository.save(pf);
    }

    @Cacheable(value = "programacion", key = "{#zonaId, #fecha}")
    public List<PartidoProgramadoDTO> obtenerProgramacion(
            Long zonaId,
            Integer fecha
    ) {
        return programacionRepository
                .findByZonaIdAndNumeroFecha(zonaId, fecha)
                .stream()
                .map(pf -> {

                    Partido p = pf.getPartido();

                    PartidoProgramadoDTO dto = new PartidoProgramadoDTO();
                    dto.setProgramacionId(pf.getId());
                    dto.setPartidoId(p.getId());

                    dto.setLocal(p.getEquipoLocal().getNombre());
                    dto.setVisitante(p.getEquipoVisitante().getNombre());
                    dto.setLocalEscudo(p.getEquipoLocal().getEscudo());
                    dto.setVisitanteEscudo(p.getEquipoVisitante().getEscudo());
                    dto.setGolesLocal(p.getGolesLocal());
                    dto.setGolesVisitante(p.getGolesVisitante());
                    dto.setEstado(p.getEstado());

                    dto.setFecha(
                            pf.getFecha() != null ? pf.getFecha().toString() : null
                    );
                    dto.setHora(
                            pf.getHora() != null ? pf.getHora().toString() : null
                    );
                    dto.setCancha(
                            pf.getCancha() != null
                                    ? pf.getCancha().getNombre()
                                    : null
                    );

                    return dto;
                })
                .toList();
    }



    public List<Integer> obtenerFechasDisponibles(Long zonaId) {
        return programacionRepository.findDistinctFechasByZonaId(zonaId);
    }

}

