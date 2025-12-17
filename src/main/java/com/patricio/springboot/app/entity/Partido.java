package com.patricio.springboot.app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipo_local_id")
    private Equipo equipoLocal;

    @ManyToOne
    @JoinColumn(name = "equipo_visitante_id")
    private Equipo equipoVisitante;

    @ManyToOne
    @JoinColumn(name = "ganador_id")
    private Equipo ganador;

    @ManyToOne
    @JoinColumn(name = "arbitro_id")
    private Arbitro arbitro;

    @OneToMany(mappedBy = "partido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadisticaJugador> estadisticas;

    @ManyToOne
    @JoinColumn(name = "cancha_id")
    private Cancha cancha;

    @ManyToOne
    @JoinColumn(name = "etapa_id")
    private EtapaTorneo etapa;  // importantísimo

    @ManyToOne
    @JoinColumn(name = "zona_id")
    private Zona zona;

    private String veedor;
    private LocalDate fecha;
    private String estado;

    private Integer golesLocal;
    private Integer golesVisitante;

    private LocalDateTime fechaHora;
    private Integer numeroFecha;
}