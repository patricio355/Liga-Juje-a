package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.EquipoZonaDTO;
import com.patricio.springboot.app.entity.EquipoZona;

public class EquipoZonaMapper {

    public static EquipoZonaDTO toDTO(EquipoZona ez) {
        EquipoZonaDTO dto = new EquipoZonaDTO();
        dto.setId(ez.getId());
        dto.setEquipoId(ez.getEquipo().getId());
        dto.setZonaId(ez.getZona().getId());
        dto.setPuntos(ez.getPuntos());
        dto.setPartidosJugados(ez.getPartidosJugados());
        dto.setGanados(ez.getGanados());
        dto.setEmpatados(ez.getEmpatados());
        dto.setPerdidos(ez.getPerdidos());
        dto.setGolesAFavor(ez.getGolesAFavor());
        dto.setGolesEnContra(ez.getGolesEnContra());
        dto.setNombreEquipo(ez.getNombreEquipo());
        return dto;
    }
}