package com.patricio.springboot.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SolicitudCierreResponse {
    private Long id;
    private Long partidoId;
    private String equipoLocal;
    private String equipoVisitante;
    private Integer golesLocal;
    private Integer golesVisitante;
    private Integer golesLocalPenales;
    private Integer golesVisitantePenales;
    private String solicitante;
    private String estado;
}
