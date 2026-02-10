package com.patricio.springboot.app.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CanchaDTO {
    private Long id;
    private String nombre;
    private String ubicacion;
    private Boolean estado = true;
    private String fotoUrl;
    private Double valorEntrada;
    private Long creador_id;
    private String ubicacionUrl;
}
