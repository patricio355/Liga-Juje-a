package com.patricio.springboot.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class TarjetaProgramacionEquipoDTO {
    private Long equipoId;
    private String equipoNombre;
    private boolean bloqueado;
    private boolean seleccionado;
    private List<OpcionPartidoDTO> opciones;
}
