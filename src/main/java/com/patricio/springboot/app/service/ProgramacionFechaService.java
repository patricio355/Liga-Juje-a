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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    /**
     * Obtiene los equipos de la zona y sus partidos disponibles (No programados aún).
     * No usamos caché aquí para que los nombres en ROJO se actualicen siempre.
     */
    public List<TarjetaProgramacionEquipoDTO> obtenerOpciones(Long zonaId, Integer numeroFecha) {
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        List<Equipo> equipos = zona.getEquiposZona()
                .stream()
                .map(EquipoZona::getEquipo)
                .toList();

        List<Partido> partidosZona = partidoRepository.findByZonaId(zonaId);

        // Partidos ya asignados a alguna fecha en esta zona
        Set<Long> partidosYaProgramados = programacionRepository.findByZonaId(zonaId)
                .stream()
                .map(pf -> pf.getPartido().getId())
                .collect(Collectors.toSet());

        Set<Long> partidosFinalizados = partidosZona.stream()
                .filter(p -> "FINALIZADO".equals(p.getEstado()))
                .map(Partido::getId)
                .collect(Collectors.toSet());

        List<TarjetaProgramacionEquipoDTO> tarjetas = new ArrayList<>();

        for (Equipo equipo : equipos) {
            TarjetaProgramacionEquipoDTO dto = new TarjetaProgramacionEquipoDTO();
            dto.setEquipoId(equipo.getId());
            dto.setEquipoNombre(equipo.getNombre());
            dto.setSeleccionado(false);

            List<OpcionPartidoDTO> opciones = partidosZona.stream()
                    .filter(p -> p.getEquipoLocal().getId().equals(equipo.getId())
                            || p.getEquipoVisitante().getId().equals(equipo.getId()))
                    .filter(p -> !partidosYaProgramados.contains(p.getId()))
                    .filter(p -> !partidosFinalizados.contains(p.getId()))
                    .map(p -> {
                        OpcionPartidoDTO o = new OpcionPartidoDTO();
                        o.setPartidoId(p.getId());
                        o.setVs(p.getEquipoLocal().getId().equals(equipo.getId())
                                ? p.getEquipoVisitante().getNombre()
                                : p.getEquipoLocal().getNombre());
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

    /**
     * ✅ PROGRAMAR PARTIDO: Limpia el caché para que el cambio sea visible.
     */
    @Transactional
    @Caching(evict = {
            // Borra el caché específico de esta fecha para que aparezca en el panel derecho
            @CacheEvict(value = "programacion", key = "{#zonaId, #fecha}"),
            // Opcional: Borra el fixture general para que se actualice en todo el sitio
            @CacheEvict(value = "torneoDetalle", allEntries = true)
    })
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

    /**
     * Obtiene los partidos programados para una fecha específica.
     * El caché se limpia automáticamente cuando se ejecuta programarPartido.
     */
    @Cacheable(value = "programacion", key = "{#zonaId, #fecha}")
    public List<PartidoProgramadoDTO> obtenerProgramacion(Long zonaId, Integer fecha) {
        return programacionRepository
                .findByZonaIdAndNumeroFecha(zonaId, fecha)
                .stream()
                .map(pf -> {
                    Partido p = pf.getPartido();
                    PartidoProgramadoDTO dto = new PartidoProgramadoDTO();
                    dto.setProgramacionId(pf.getId());
                    dto.setPartidoId(p.getId());

                    // Datos de equipos y ESCUDOS
                    dto.setLocal(p.getEquipoLocal().getNombre());
                    dto.setVisitante(p.getEquipoVisitante().getNombre());
                    dto.setLocalEscudo(p.getEquipoLocal().getEscudo());
                    dto.setVisitanteEscudo(p.getEquipoVisitante().getEscudo());

                    dto.setGolesLocal(p.getGolesLocal());
                    dto.setGolesVisitante(p.getGolesVisitante());
                    dto.setEstado(p.getEstado());

                    dto.setFecha(pf.getFecha() != null ? pf.getFecha().toString() : null);
                    dto.setHora(pf.getHora() != null ? pf.getHora().toString() : null);

                    // Si el registro de programación no tiene cancha, usamos la del local
                    if (pf.getCancha() != null) {
                        dto.setCancha(pf.getCancha().getNombre());
                    } else if (p.getEquipoLocal().getLocalia() != null) {
                        dto.setCancha(p.getEquipoLocal().getLocalia().getNombre());
                    }

                    return dto;
                })
                .toList();
    }

    public List<Integer> obtenerFechasDisponibles(Long zonaId) {
        return programacionRepository.findDistinctFechasByZonaId(zonaId);
    }
}