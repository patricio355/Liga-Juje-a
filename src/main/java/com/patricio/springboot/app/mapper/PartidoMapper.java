package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.PartidoDTO;
import com.patricio.springboot.app.entity.Partido;

public class PartidoMapper {

    public static PartidoDTO toDTO(Partido p) {
        PartidoDTO dto = new PartidoDTO();

        dto.setId(p.getId());

        dto.setEquipoLocalId(p.getEquipoLocal().getId());
        dto.setEquipoLocalNombre(p.getEquipoLocal().getNombre());

        dto.setEquipoVisitanteId(p.getEquipoVisitante().getId());
        dto.setEquipoVisitanteNombre(p.getEquipoVisitante().getNombre());

        if (p.getCancha() != null) {
            dto.setCanchaId(p.getCancha().getId());
            dto.setCanchaNombre(p.getCancha().getNombre());
        }

        if (p.getZona() != null) {
            dto.setZonaId(p.getZona().getId());
            dto.setZonaNombre(p.getZona().getNombre());
        }

        if (p.getEtapa() != null) {
            dto.setEtapaId(p.getEtapa().getId());
            dto.setEtapaNombre(p.getEtapa().getNombre());
        }

        if (p.getArbitro() != null) {
            dto.setArbitroId(p.getArbitro().getId());
            dto.setArbitroNombre(p.getArbitro().getNombre());
        }

        dto.setVeedor(p.getVeedor());
        dto.setFecha(p.getFecha().toString());
        dto.setEstado(p.getEstado());

        return dto;
    }
}