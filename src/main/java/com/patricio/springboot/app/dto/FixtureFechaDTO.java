package com.patricio.springboot.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class FixtureFechaDTO {

    private Integer numeroFecha;
    private List<PartidoDTO> partidos;

    public FixtureFechaDTO(Integer numeroFecha, List<PartidoDTO> partidos) {
        this.numeroFecha = numeroFecha;
        this.partidos = partidos;
    }
}
