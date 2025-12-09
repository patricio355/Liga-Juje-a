package com.patricio.springboot.app.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class JugadorDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private Integer dni;
    private Byte carnetPdf;
    private LocalDate fechaAlta;
    private LocalDate fechaBaja;
    private String estado;
    private String posicion;
    private boolean federado;

    private Long idEquipo;       // solo el ID, sin traer todo el equipo
    private String nombreEquipo; // para mostrar en el front si querés
}


