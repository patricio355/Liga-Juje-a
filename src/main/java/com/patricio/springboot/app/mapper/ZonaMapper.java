
package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.Zona;

public class ZonaMapper {

    public static ZonaDTO toDTO(Zona zona) {
        ZonaDTO dto = new ZonaDTO();
        dto.setId(zona.getId());
        dto.setNombre(zona.getNombre());
        dto.setDescripcion(zona.getDescripcion());
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