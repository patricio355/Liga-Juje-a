package com.patricio.springboot.app.entity;


import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String contrasenia;
    private String dni;
    private String telefono;
    private String domicilio;

    @Column(nullable = false)
    private String rol; // ADMIN, ENCARGADO, ENCARGADOTORNEO, ARBITRO, VEEDOR

    private boolean activo = true;
}

