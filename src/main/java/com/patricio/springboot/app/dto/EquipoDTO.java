package com.patricio.springboot.app.dto;

import com.patricio.springboot.app.entity.EncargadoEquipo;
import com.patricio.springboot.app.entity.Zona;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class EquipoDTO {
    private Long id;
    private Long equipoZonaId;
    private String nombre;
    private String localidad;
    private String escudo;
    private String camisetaTitular;
    private String camisetaSuplente;
    private Boolean estado;
    private String fechaCreacion;

    private List<EquipoZonaDTO> inscripciones;

    private Long zonaId;
    private Long canchaId;
    private String encargadoEmail;
    private Long creadorId;
    private String creadorEmail;

    public EquipoDTO(Long id, String nombre, String escudo) {
    }
}
