package com.patricio.springboot.app.dto;

import lombok.Data;

@Data
public class EstadisticaJugadorDTO {
    private Long partidoId;
    private Long jugadorId;
    private int goles;
    private int amarillas;
    private int rojas;
}
