package com.patricio.springboot.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class TarjetaProgramacionEquipoDTO {
    private Long equipoId;
    private String equipoNombre;
    private String escudo;
    private boolean bloqueado;
    private boolean seleccionado;
    private List<OpcionPartidoDTO> opciones;
}
