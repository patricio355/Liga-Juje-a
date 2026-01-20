package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.*;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.Torneo;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TorneoMapper {

    public static TorneoDTO toDTO(Torneo torneo) {
        if (torneo == null) return null;

        TorneoDTO dto = new TorneoDTO();
        dto.setId(torneo.getId());
        dto.setNombre(torneo.getNombre());
        dto.setDivision(torneo.getDivision());
        dto.setEncargadoEmail(
                torneo.getEncargado() != null ? torneo.getEncargado().getEmail() : null
        );
        if (torneo.getEncargado() != null) {
            dto.setEncargadoTelefono(torneo.getEncargado().getTelefono());
        }
        dto.setEstado(torneo.getEstado());
        dto.setFechaCreacion(torneo.getFechaCreacion());
        dto.setTipo(torneo.getTipo());
        dto.setSlug(torneo.getSlug());
        dto.setCreadorEmail(torneo.getCreador().getEmail());
        dto.setCreadorId(torneo.getCreador().getId());
        dto.setPuntosEmpate(torneo.getPuntosEmpate());
        dto.setPuntosGanador(torneo.getPuntosGanador());

        dto.setFotoUrl(torneo.getFotoUrl());
        dto.setGenero(torneo.getGenero());
        dto.setRedSocial(torneo.getRedSocial());

        // --- NUEVOS ATRIBUTOS DE COLOR ---
        dto.setColorPrimario(torneo.getColorPrimario());
        dto.setColorSecundario(torneo.getColorSecundario());
        dto.setColorTextoPrimario(torneo.getColorTextoPrimario());
        dto.setColorTextoSecundario(torneo.getColorTextoSecundario());
        // --------------------------------

        if (torneo.getZonas() != null) {
            dto.setZonas(
                    torneo.getZonas().stream()
                            .map(z -> {
                                ZonaDTO zd = new ZonaDTO();
                                zd.setId(z.getId());
                                zd.setNombre(z.getNombre());
                                zd.setDescripcion(z.getDescripcion());

                                // 1. MAPEAMOS LOS EQUIPOS (Relación intermedia)
                                if (z.getEquiposZona() != null) {
                                    zd.setEquipos(z.getEquiposZona().stream()
                                            .map(ez -> {
                                                Equipo e = ez.getEquipo();
                                                EquipoDTO edto = new EquipoDTO();
                                                edto.setId(e.getId());
                                                edto.setNombre(e.getNombre());
                                                edto.setEscudo(e.getEscudo());
                                                edto.setEstado(e.isEstado());
                                                edto.setEquipoZonaId(ez.getId());
                                                return edto;
                                            })
                                            .collect(Collectors.toList())
                                    );
                                } else {
                                    zd.setEquipos(new ArrayList<>());
                                }

                                // 2. MAPEAMOS LOS PARTIDOS
                                if (z.getPartidos() != null) {
                                    zd.setPartidos(z.getPartidos().stream()
                                            .map(p -> {
                                                PartidoDTO pdto = new PartidoDTO();
                                                pdto.setId(p.getId());
                                                return pdto;
                                            })
                                            .collect(Collectors.toList())
                                    );
                                } else {
                                    zd.setPartidos(new ArrayList<>());
                                }

                                return zd;
                            })
                            .toList()
            );
        }

        return dto;
    }


    public static Torneo toEntity(TorneoDTO dto) {
        Torneo t = new Torneo();
        t.setNombre(dto.getNombre());
        t.setDivision(dto.getDivision());
        t.setEstado(dto.getEstado());
        t.setTipo(dto.getTipo());
        t.setPuntosGanador(dto.getPuntosGanador());
        t.setPuntosEmpate(dto.getPuntosEmpate());

        // --- NUEVOS ATRIBUTOS DE COLOR ---
        t.setColorPrimario(dto.getColorPrimario());
        t.setColorSecundario(dto.getColorSecundario());
        t.setColorTextoPrimario(dto.getColorTextoPrimario());
        t.setColorTextoSecundario(dto.getColorTextoSecundario());
        // --------------------------------
        t.setFotoUrl(dto.getFotoUrl());
        t.setGenero(dto.getGenero());
        t.setRedSocial(dto.getRedSocial());


        return t;
    }


    public static TorneoResumenDTO toResumenDTO(Torneo torneo) {
        if (torneo == null) return null;
        TorneoResumenDTO dto = new TorneoResumenDTO();
        dto.setId(torneo.getId());
        dto.setNombre(torneo.getNombre());
        dto.setSlug(torneo.getSlug());
        dto.setDivision(torneo.getDivision());
        dto.setEstado(torneo.getEstado());
        dto.setFotoUrl(torneo.getFotoUrl());
        dto.setGenero(torneo.getGenero());
        dto.setColorPrimario(torneo.getColorPrimario());
        dto.setColorSecundario(torneo.getColorSecundario());
        dto.setColorTextoPrimario(torneo.getColorTextoPrimario());
        dto.setColorTextoSecundario(torneo.getColorTextoSecundario());
        return dto;
    }
}