package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.PartidoDTO;
import com.patricio.springboot.app.entity.Partido;

public class PartidoMapper {

    private PartidoMapper() {}

    public static PartidoDTO toDTO(Partido p) {
        if (p == null) return null;

        PartidoDTO dto = new PartidoDTO();

        dto.setId(p.getId());

        // --- MANEJO SEGURO EQUIPO LOCAL ---
        if (p.getEquipoLocal() != null) {
            dto.setEquipoLocalId(p.getEquipoLocal().getId());
            dto.setEquipoLocalNombre(p.getEquipoLocal().getNombre());
            dto.setEquipoLocalEscudo(p.getEquipoLocal().getEscudo()); // Movido aquí
        } else {
            dto.setEquipoLocalNombre("POR DEFINIR");
            dto.setEquipoLocalEscudo(null);
        }

        // --- MANEJO SEGURO EQUIPO VISITANTE ---
        if (p.getEquipoVisitante() != null) {
            dto.setEquipoVisitanteId(p.getEquipoVisitante().getId());
            dto.setEquipoVisitanteNombre(p.getEquipoVisitante().getNombre());
            dto.setEquipoVisitanteEscudo(p.getEquipoVisitante().getEscudo()); // Movido aquí
        } else {
            dto.setEquipoVisitanteNombre("POR DEFINIR");
            dto.setEquipoVisitanteEscudo(null);
        }

        // Ganador
        if (p.getGanador() != null) {
            dto.setGanadorId(p.getGanador().getId());
            dto.setGanadorNombre(p.getGanador().getNombre());
        }

        // Árbitro
        if (p.getArbitro() != null) {
            dto.setArbitroId(p.getArbitro().getId());
            dto.setArbitro(p.getArbitro().getNombre());
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
            dto.setFecha(p.getFecha().toString());
        }

        // Hora (LocalTime)
        if (p.getHora() != null) {
            dto.setHora(p.getHora().toString());
        }

        dto.setGolesLocal(p.getGolesLocal());
        dto.setGolesVisitante(p.getGolesVisitante());
        dto.setGolesLocalPenales(p.getGolesLocalPenales());
        dto.setGolesVisitantePenales(p.getGolesVisitantePenales());

        dto.setNumeroFecha(p.getNumeroFecha());
        dto.setEstado(p.getEstado());

        dto.setOrden(p.getOrden());

        return dto;
    }
}