package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    private EncargadoEquipo encargado;

    @ManyToOne
    @JoinColumn(name = "alta_jugador_id")
    private Jugador altaJugador;

    @ManyToOne
    @JoinColumn(name = "baja_jugador_id")
    private Jugador bajaJugador;

    private String estado;

    private LocalDate fecha;

    private String fotoUrl;
}