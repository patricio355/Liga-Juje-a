package com.patricio.springboot.app.entity;


import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String contrasenia;
    private String email;
    private String telefono;
    private String dni;
    private String domicilio;
}
