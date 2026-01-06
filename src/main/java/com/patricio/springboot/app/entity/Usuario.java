package com.patricio.springboot.app.entity;


import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol", discriminatorType = DiscriminatorType.STRING)
@Data
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

    @Column(name = "rol", insertable = false, updatable = false)
    private String rol; // ADMIN, ENCARGADO, ENCARGADOTORNEO, ARBITRO, VEEDOR

    private boolean activo = true;
}

