package com.patricio.springboot.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PartidoCreateDTO {

    private Long equipoLocalId;
    private Long equipoVisitanteId;

    private Long canchaId;
    private Long zonaId;
    private Long etapaId;
    private Integer numeroFecha;
    private LocalDateTime fechaHora;
    private Long arbitroId;

    private String veedor;
    private String fecha;  // formato yyyy-MM-dd
}