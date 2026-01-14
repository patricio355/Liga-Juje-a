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

    @Column(name = "puntos_ganador")
    private Integer puntosGanador = 3; // Valor por defecto

    @Column(name = "puntos_empate")
    private Integer puntosEmpate = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @Column(name = "color_primario")
    private String colorPrimario = "#0a0c10"; // Fondo oscuro por defecto

    @Column(name = "color_secundario")
    private String colorSecundario = "#1e293b"; // Gris azulado metálico

    @Column(name = "color_texto_primario")
    private String colorTextoPrimario = "#f1f5f9"; // Blanco humo

    @Column(name = "color_texto_secundario")
    private String colorTextoSecundario = "#94a3b8"; // Gris plata

    @Column(name = "foto_url")
    private String fotoUrl; // URL del logo o banner

    @Column(name = "genero")
    private String genero; // MASCULINO, FEMENINO, MIXTO

    @Column(name = "red_social")
    private String redSocial;
}