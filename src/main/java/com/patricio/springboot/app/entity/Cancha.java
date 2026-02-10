package com.patricio.springboot.app.entity;

import com.sun.jdi.request.StepRequest;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "canchas")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder // Útil para crear objetos en tus servicios o tests
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String ubicacion;


    private boolean estado = true; // Por defecto activa

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "valor_entrada")
    private Double valorEntrada;

    private String ubicacionUrl;

    @ManyToOne(fetch = FetchType.LAZY) // Relación con el usuario creador
    @JoinColumn(name = "creador_id") // Nombre de la columna en la DB
    private Usuario creador;

    // Auditoría básica (Opcional pero recomendada para Analistas)
    @Column(name = "fecha_creacion", updatable = false)
    private java.time.LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = java.time.LocalDateTime.now();
        this.estado = true;
    }
}