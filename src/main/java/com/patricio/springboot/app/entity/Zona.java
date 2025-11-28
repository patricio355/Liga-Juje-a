package com.patricio.springboot.app.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "zonas")
public class Zona {
    @Id
    private Long id;
    private String nombre;

    @OneToMany(mappedBy = "zona")
    private List<Equipo> equipos;

    private String descripcion;


    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partido> partidos;

    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;
}
