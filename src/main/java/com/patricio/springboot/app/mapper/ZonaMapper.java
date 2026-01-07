
package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.Zona;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ZonaMapper {

    public static ZonaDTO toDTO(Zona zona) {
            ZonaDTO dto = new ZonaDTO();
            dto.setId(zona.getId());
            dto.setNombre(zona.getNombre());
            dto.setTorneoId(zona.getTorneo().getId());
            dto.setTorneoNombre(zona.getTorneo().getNombre());

            // IMPORTANTE: Extraer los equipos de la relación intermedia
            if (zona.getEquiposZona() != null) {
                dto.setEquipos(zona.getEquiposZona().stream()
                        .map(ez -> {
                            Equipo e = ez.getEquipo();
                            // Retornamos un objeto simple que el front entienda
                            return new EquipoDTO(e.getId(), e.getNombre(), e.getEscudo());
                        })
                        .collect(Collectors.toList()));
            } else {
                dto.setEquipos(new ArrayList<>());
            }
            return dto;
        }

    public static Zona toEntity(ZonaDTO dto) {
        Zona zona = new Zona();
        zona.setId(dto.getId());
        zona.setNombre(dto.getNombre());
        zona.setDescripcion(dto.getDescripcion());
        return zona;
    }
}