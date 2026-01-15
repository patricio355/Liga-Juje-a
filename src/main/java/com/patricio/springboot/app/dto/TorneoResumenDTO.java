package com.patricio.springboot.app.dto;

import lombok.Data;

@Data
public class TorneoResumenDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String division;
    private String estado;
    private String fotoUrl;
    private String genero;
    private String colorPrimario;
    private String colorSecundario;
    private String colorTextoPrimario;
    private String colorTextoSecundario;
    // IMPORTANTE: No incluimos List<ZonaDTO> aquí

    // Getters y Setters...
}