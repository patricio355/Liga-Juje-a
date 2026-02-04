package com.patricio.springboot.app.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PartidoCreateDTO {

    private Long equipoLocalId;
    private Long equipoVisitanteId;

    private Long canchaId;
    private Long zonaId;
    private Long torneoId; // Sugerido para validaciones rápidas

    // Campo clave para tu nueva lógica
    private Long etapaId;

    private Integer numeroFecha; // En fase final puede ser 1 (único partido)

    private LocalDate fecha; // Formato YYYY-MM-DD
    private LocalTime hora;  // Formato HH:mm

    private Long arbitroId;
    private String veedor;

    // Para el control del cuadro de Brackets
    private String llaveId;
    private Integer orden;
}