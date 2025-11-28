package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.entity.Equipo;

public final class EquipoMapper {

    private EquipoMapper() {}

    public static EquipoDTO toDTO(Equipo e) {
        EquipoDTO dto = new EquipoDTO();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setLocalidad(e.getLocalidad());
        dto.setEscudo(e.getEscudo());
        dto.setCamisetaTitular(e.getCamisetaTitular());
        dto.setCamisetaSuplente(e.getCamisetaSuplente());
        dto.setEstado(e.getEstado());
        dto.setFechaCreacion(e.getFechaCreacion());

        dto.setEncargadoNombre(e.getEncargado().getNombre());

        dto.setZonaId(e.getZona().getId());

        return dto;
    }
}
