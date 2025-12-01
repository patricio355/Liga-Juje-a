package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
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


    @OneToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "encargado_id")
    private Encargado encargado;

    private String escudo;
    private String camisetaTitular;
    private String camisetaSuplente;
    private LocalDate fechaCreacion;
    private boolean estado;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "cancha_id",nullable = true)
    private Cancha localia;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "zona_id")
    private Zona zona;

}
