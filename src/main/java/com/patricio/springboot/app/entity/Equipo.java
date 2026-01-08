package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "equipos")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String localidad;

    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL)
    private List<Jugador> jugadores;

    @OneToMany(mappedBy = "equipo")
    private List<EquipoZona> participaciones;

    @OneToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "encargado_id")
    private Usuario encargado;

    private String escudo;
    private String camisetaTitular;
    private String camisetaSuplente;
    private LocalDate fechaCreacion;
    private boolean estado = true;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "cancha_id",nullable = true)
    private Cancha localia;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    public void addJugador(Jugador jugador) {
        if (this.jugadores == null) {
            this.jugadores = new ArrayList<>();
        }
        this.jugadores.add(jugador);
    }
}
