package com.patricio.springboot.app.dto;

import lombok.Data;

@Data
public class PartidoCreateDTO {

    private Long equipoLocalId;
    private Long equipoVisitanteId;

    private Long canchaId;
    private Long zonaId;
    private Long etapaId;
    private Integer numeroFecha;

    private String fecha;


    private String hora;

    private Long arbitroId;
    private String veedor;
}