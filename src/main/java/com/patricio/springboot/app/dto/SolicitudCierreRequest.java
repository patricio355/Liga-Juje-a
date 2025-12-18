package com.patricio.springboot.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudCierreRequest {
    private Integer golesLocal;
    private Integer golesVisitante;
}
