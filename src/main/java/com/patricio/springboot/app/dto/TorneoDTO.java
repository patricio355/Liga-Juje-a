package com.patricio.springboot.app.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TorneoDTO {
    private Long id;
    private String nombre;
    private String division;
    private String encargado;
    private String estado;
    private LocalDate fechaCreacion;
    private List<ZonaDTO> zonas;
}