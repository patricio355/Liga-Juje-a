package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "canchas")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Cancha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String ubicacion;
    private byte habilitacionPdf;
    private boolean estado;
    private String fotoUrl;
    private int valorEntrada;


}
