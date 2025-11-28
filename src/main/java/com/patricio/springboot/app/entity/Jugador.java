package com.patricio.springboot.app.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "jugadores")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Jugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String apellido;
    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;
    private byte carnetPdf;
    private int  dni;
    private LocalDate fechaAlta = LocalDate.now();
    private LocalDate fechaBaja;
    private String estado;
    private String posicion;
    private boolean federado;


}
