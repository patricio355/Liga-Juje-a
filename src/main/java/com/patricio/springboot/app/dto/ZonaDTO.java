package com.patricio.springboot.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class ZonaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<EquipoDTO> equipos;
    private List<PartidoDTO> partidos;
}