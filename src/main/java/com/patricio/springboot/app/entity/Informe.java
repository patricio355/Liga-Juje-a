package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;




@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "informes")
public class Informe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario informador;
    private String detalle;
    private String fotoUrl;
    private String estado;
    private LocalDate fecha;

}
