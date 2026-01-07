package com.patricio.springboot.app.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Table(name = "torneos")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String division;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Zona> zonas;

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    private Usuario encargado;

    private String estado;

    private LocalDate fechaCreacion;

    private String tipo;

    @Column(unique = true, nullable = false)
    private String slug;
}