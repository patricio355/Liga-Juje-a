package com.patricio.springboot.app.entity;


import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// Usamos "rol" como discriminador
@DiscriminatorColumn(name = "rol", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Usuario") // Valor por defecto si no se especifica otro
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creador")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Usuario creador;
}

