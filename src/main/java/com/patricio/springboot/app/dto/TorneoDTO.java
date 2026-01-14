package com.patricio.springboot.app.dto;

import com.patricio.springboot.app.entity.EncargadoEquipo;
import com.patricio.springboot.app.entity.Usuario;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TorneoDTO {
    private Long id;
    private String nombre;
    private String division;
    private String encargadoEmail;
    private String estado;
    private LocalDate fechaCreacion;
    private List<ZonaDTO> zonas;
    private String tipo;
    private String slug;
    private Integer puntosGanador;
    private Integer puntosEmpate;
    private Long creadorId;
    private String creadorEmail;

    private String colorPrimario;// Fondo oscuro por defecto

    private String colorSecundario; // Gris azulado metálico

    private String colorTextoPrimario; // Blanco humo

    private String colorTextoSecundario; // Gris plata
    private String fotoUrl;
    private String genero;
    private String redSocial;
    private String encargadoTelefono;
// Getters y Setters...

}