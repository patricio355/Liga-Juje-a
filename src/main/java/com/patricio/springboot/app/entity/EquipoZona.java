package com.patricio.springboot.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipo_zona")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @ManyToOne
    @JoinColumn(name = "zona_id")
    private Zona zona;

    // Campos para tabla de posiciones
    private int puntos = 0;
    private int partidosJugados = 0;
    private int ganados = 0;
    private int empatados = 0;
    private int perdidos = 0;
    private int golesAFavor = 0;
    private int golesEnContra = 0;
    private String nombreEquipo;
    public int getDiferencia() {
        return golesAFavor - golesEnContra;
    }
}

