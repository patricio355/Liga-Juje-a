package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.PartidoDTO;
import com.patricio.springboot.app.entity.Partido;

public class PartidoMapper {

    private PartidoMapper() {}

    public static PartidoDTO toDTO(Partido p) {
        if (p == null) return null;

        PartidoDTO dto = new PartidoDTO();

        dto.setId(p.getId());

        dto.setEquipoLocalEscudo(p.getEquipoLocal().getEscudo());
        dto.setEquipoVisitanteEscudo(p.getEquipoVisitante().getEscudo());


        // Equipo local
        if (p.getEquipoLocal() != null) {
            dto.setEquipoLocalId(p.getEquipoLocal().getId());
            dto.setEquipoLocalNombre(p.getEquipoLocal().getNombre());
        }

        // Equipo visitante
        if (p.getEquipoVisitante() != null) {
            dto.setEquipoVisitanteId(p.getEquipoVisitante().getId());
            dto.setEquipoVisitanteNombre(p.getEquipoVisitante().getNombre());
        }

        // Ganador
        if (p.getGanador() != null) {
            dto.setGanadorId(p.getGanador().getId());
            dto.setGanadorNombre(p.getGanador().getNombre());
        }

        // Árbitro
        if (p.getArbitro() != null) {
            dto.setArbitroId(p.getArbitro().getId());
            dto.setArbitroNombre(p.getArbitro().getNombre());
        }

        // Cancha
        if (p.getCancha() != null) {
            dto.setCanchaId(p.getCancha().getId());
            dto.setCanchaNombre(p.getCancha().getNombre());
        }

        // Zona
        if (p.getZona() != null) {
            dto.setZonaId(p.getZona().getId());
            dto.setZonaNombre(p.getZona().getNombre());
        }

        // Etapa
        if (p.getEtapa() != null) {
            dto.setEtapaId(p.getEtapa().getId());
            dto.setEtapaNombre(p.getEtapa().getNombre());
        }

        dto.setVeedor(p.getVeedor());

        // Fecha (LocalDate)
        if (p.getFecha() != null) {
            dto.setFecha(p.getFecha().toString()); // yyyy-MM-dd
        }

        // FechaHora (LocalDateTime)
        if (p.getFechaHora() != null) {
            dto.setFechaHora(p.getFechaHora().toString()); // ISO
        }

        dto.setGolesLocal(p.getGolesLocal());
        dto.setGolesVisitante(p.getGolesVisitante());

        dto.setNumeroFecha(p.getNumeroFecha());
        dto.setEstado(p.getEstado());

        return dto;
    }
}
