package com.patricio.springboot.app.dto;

import lombok.Data;

@Data
public class PartidoDTO {

    private Long id;

    private Long equipoLocalId;
    private String equipoLocalNombre;

    private Long equipoVisitanteId;
    private String equipoVisitanteNombre;

    private Integer golesLocal;
    private Integer golesVisitante;

    private Long canchaId;
    private String canchaNombre;

    private Long zonaId;
    private String zonaNombre;

    private Long etapaId;
    private String etapaNombre;

    private Long arbitroId;
    private String arbitroNombre;

    private String veedor;

    private String fecha; // formato yyyy-MM-dd
    private String estado;
}