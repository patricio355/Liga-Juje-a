package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.TorneoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.Torneo;

public class TorneoMapper {
    public static TorneoDTO toDTO(Torneo torneo) {
        TorneoDTO dto = new TorneoDTO();
        dto.setId(torneo.getId());
        dto.setNombre(torneo.getNombre());
        dto.setDivision(torneo.getDivision());
        dto.setEncargado(torneo.getEncargado());
        dto.setEstado(torneo.getEstado());
        dto.setFechaCreacion(torneo.getFechaCreacion());

        if (torneo.getZonas() != null) {
            dto.setZonas(
                    torneo.getZonas().stream()
                            .map(z -> {
                                ZonaDTO zd = new ZonaDTO();
                                zd.setId(z.getId());
                                zd.setNombre(z.getNombre());
                                return zd;
                            })
                            .toList()
            );
        }

        return dto;
    }
}
