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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;



    private String descripcion;


    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partido> partidos;

    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL)
    private List<EquipoZona> equiposZona;

    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;
}
