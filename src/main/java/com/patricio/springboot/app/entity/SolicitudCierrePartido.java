package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class SolicitudCierrePartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Partido partido;

    @ManyToOne
    private Usuario solicitante; // arbitro o veedor

    private Integer golesLocal;
    private Integer golesVisitante;

    private String estado;
    // PENDIENTE, APROBADA, RECHAZADA

    private LocalDateTime fechaSolicitud;
}