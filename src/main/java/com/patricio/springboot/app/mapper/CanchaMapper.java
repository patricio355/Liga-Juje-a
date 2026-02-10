package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.CanchaDTO;
import com.patricio.springboot.app.entity.Cancha;

public class CanchaMapper {

    public CanchaMapper() {}

    public static CanchaDTO toDTO(Cancha c) {
        CanchaDTO dto = new CanchaDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setEstado(c.isEstado());
        dto.setUbicacion(c.getUbicacion());
        dto.setFotoUrl(c.getFotoUrl());
        dto.setValorEntrada(c.getValorEntrada());
        dto.setUbicacionUrl(c.getUbicacionUrl());

        dto.setCreador_id(c.getCreador().getId());

        return dto;
    }

    public static Cancha toEntity(CanchaDTO dto) {
        Cancha c = new Cancha();
        c.setId(dto.getId());
        c.setNombre(dto.getNombre());
        c.setEstado(dto.getEstado());
        c.setUbicacion(dto.getUbicacion());
        c.setFotoUrl(dto.getFotoUrl());
        c.setValorEntrada(dto.getValorEntrada());
        c.setUbicacionUrl(dto.getUbicacionUrl());
        c.setEstado(dto.getEstado() != null && dto.getEstado());
        return c;
    }
}
