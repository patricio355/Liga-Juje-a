package com.patricio.springboot.app.dto;

import lombok.Data;
@Data
public class PartidoProgramadoDTO {

    private Long programacionId;
    private Long partidoId;

    private String local;
    private String visitante;
    private String localEscudo;
    private String visitanteEscudo;

    private Integer golesLocal;
    private Integer golesVisitante;

    private String estado; // FINALIZADO / PENDIENTE

    private String fecha;
    private String hora;
    private String cancha;
}