package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToOne(cascade = CascadeType.ALL)
    private Encargado encargado;

    private String escudo;
    private String camisetaTitular;
    private String camisetaSuplente;
    private String fechaCreacion;
    private String estado;

    @ManyToOne
    @JoinColumn(name = "cancha_id")
    private Cancha localia;

    @ManyToOne
    @JoinColumn(name = "zona_id")
    private Zona zona;

}
