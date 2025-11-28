package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "estadisticas_jugador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaJugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int goles;
    private int rojas;
    private int amarillas;
    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;
    @ManyToOne
    @JoinColumn(name = "partido_id")
    private Partido partido;
    private String observacion;

}
