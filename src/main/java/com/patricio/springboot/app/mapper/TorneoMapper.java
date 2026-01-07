package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.dto.PartidoDTO;
import com.patricio.springboot.app.dto.TorneoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
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
        dto.setEstado(torneo.getEstado());
        dto.setFechaCreacion(torneo.getFechaCreacion());
        dto.setTipo(torneo.getTipo());
        dto.setSlug(torneo.getSlug());
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
                                                edto.setEquipoZonaId(ez.getId()); //
                                                return edto;
                                            })
                                            .collect(Collectors.toList())
                                    );
                                } else {
                                    zd.setEquipos(new ArrayList<>());
                                }

                                // 2. IMPORTANTE: MAPEAMOS LOS PARTIDOS (Para habilitar el bloqueo en el Front)
                                // Si esta lista tiene elementos, el Front ocultará los botones de edición
                                if (z.getPartidos() != null) {
                                    zd.setPartidos(z.getPartidos().stream()
                                            .map(p -> {
                                                // Solo enviamos IDs o datos mínimos para validar existencia
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
        return t;
    }
}
