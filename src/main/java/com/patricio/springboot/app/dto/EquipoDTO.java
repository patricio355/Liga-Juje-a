package com.patricio.springboot.app.dto;

import com.patricio.springboot.app.entity.Encargado;
import com.patricio.springboot.app.entity.Zona;
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
    private Boolean estado;
    private String fechaCreacion;

    private Long zonaId;
    private Long canchaId;
    private Long encargadoId;
}
