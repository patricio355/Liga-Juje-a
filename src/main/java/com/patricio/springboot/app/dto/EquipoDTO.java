package com.patricio.springboot.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class EquipoDTO {
    private Long id;
    private String nombre;
    private String localidad;
    private String escudo;
    private String camisetaTitular;
    private String camisetaSuplente;
    private String estado;
    private String fechaCreacion;

    private String encargadoNombre;
    private Long zonaId;
}
