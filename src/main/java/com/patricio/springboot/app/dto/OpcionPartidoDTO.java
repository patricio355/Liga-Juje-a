package com.patricio.springboot.app.dto;

import lombok.Data;

@Data
public class OpcionPartidoDTO {
    private Long partidoId;
    private String vs;
    private boolean jugado;
}
