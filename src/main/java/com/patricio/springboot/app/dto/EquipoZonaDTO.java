package com.patricio.springboot.app.dto;

import lombok.Data;

@Data
public class EquipoZonaDTO {
    private Long id;
    private Long equipoId;
    private Long zonaId;
    private Long torneoId;
    private String nombreTorneo;
    private String nombreZona;
    private String escudo;
    private int puntos;
    private int partidosJugados;
    private int ganados;
    private int empatados;
    private int perdidos;
    private int golesAFavor;
    private int golesEnContra;
    private String nombreEquipo;
}