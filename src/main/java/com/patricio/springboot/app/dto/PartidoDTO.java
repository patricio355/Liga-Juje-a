package com.patricio.springboot.app.dto;

import lombok.Data;

@Data
public class PartidoDTO {

    private Long id;

    // Equipos
    private Long equipoLocalId;
    private String equipoLocalNombre;
    private String equipoLocalEscudo;

    private Long equipoVisitanteId;
    private String equipoVisitanteNombre;
    private String equipoVisitanteEscudo;

    private Long ganadorId;
    private String ganadorNombre;

    // Arbitro
    private Long arbitroId;
    private String arbitroNombre;

    // Cancha
    private Long canchaId;
    private String canchaNombre;

    // Zona
    private Long zonaId;
    private String zonaNombre;

    // Etapa
    private Long etapaId;
    private String etapaNombre;

    private Integer golesLocal;
    private Integer golesVisitante;

    // Otros
    private String veedor;

    private String fecha;       // yyyy-MM-dd (LocalDate)
    private String fechaHora;   // ISO (LocalDateTime)

    private Integer numeroFecha;
    private String estado;
}
