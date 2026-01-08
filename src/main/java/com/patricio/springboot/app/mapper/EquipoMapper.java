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
        dto.setEstado(e.isEstado());
        dto.setFechaCreacion(e.getFechaCreacion().toString());
        dto.setCanchaId(e.getLocalia() != null ? e.getLocalia().getId() : null);
        dto.setEncargadoEmail(e.getEncargado() != null ? e.getEncargado().getEmail() : null);
        dto.setCreadorId(e.getCreador() != null ? e.getCreador().getId() : null);
        dto.setCreadorEmail(e.getCreador() != null ? e.getCreador().getEmail() : null);
        return dto;
    }

    public static Equipo toEntity(EquipoDTO dto) {
        Equipo e = new Equipo();
        e.setNombre(dto.getNombre());
        e.setLocalidad(dto.getLocalidad());
        e.setEscudo(dto.getEscudo());
        e.setCamisetaTitular(dto.getCamisetaTitular());
        e.setCamisetaSuplente(dto.getCamisetaSuplente());
        e.setEstado(dto.getEstado());
        return e;
    }
}
