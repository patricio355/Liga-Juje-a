package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.JugadorDTO;
import com.patricio.springboot.app.entity.Jugador;

public class JugadorMapper {



    public static JugadorDTO toDTO(Jugador jugador) {
        if (jugador == null) {
            return null;
        }

        JugadorDTO dto = new JugadorDTO();
        dto.setId(jugador.getId());
        dto.setNombre(jugador.getNombre());
        dto.setApellido(jugador.getApellido());
        dto.setDni(jugador.getDni());
        dto.setCarnetPdf(jugador.getCarnetPdf());
        dto.setFechaAlta(jugador.getFechaAlta());
        dto.setFechaBaja(jugador.getFechaBaja());
        dto.setEstado(jugador.getEstado());
        dto.setPosicion(jugador.getPosicion());
        dto.setFederado(jugador.isFederado());

        // equipo (opcional)
        if (jugador.getEquipo() != null) {
            dto.setIdEquipo(jugador.getEquipo().getId());
            dto.setNombreEquipo(jugador.getEquipo().getNombre());
        }

        return dto;
    }

    public static Jugador toEntity(JugadorDTO dto) {
        if (dto == null) return null;

        Jugador jugador = new Jugador();


        if (dto.getId() != null) {
            jugador.setId(dto.getId());
        }

        jugador.setNombre(dto.getNombre());
        jugador.setApellido(dto.getApellido());
        jugador.setDni(dto.getDni());
        jugador.setCarnetPdf(dto.getCarnetPdf() != null ? dto.getCarnetPdf() : 0);


        if (dto.getFechaAlta() != null) {
            jugador.setFechaAlta(dto.getFechaAlta());
        }

        jugador.setFechaBaja(dto.getFechaBaja());
        jugador.setEstado(dto.getEstado());
        jugador.setPosicion(dto.getPosicion());
        jugador.setFederado(dto.isFederado());



        return jugador;
    }

}
