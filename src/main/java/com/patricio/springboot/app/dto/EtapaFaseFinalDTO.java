package com.patricio.springboot.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class EtapaFaseFinalDTO {
    private Long id;
    private String nombre;
    private Integer orden;
    private List<PartidoResumenDTO> partidos;

    @Data
    public static class PartidoResumenDTO {
        private Long id;
        private String equipoLocal;
        private String equipoLocalEscudo;
        private String equipoVisitante;
        private String equipoVisitanteEscudo;
        private Integer golesLocal;
        private Integer golesVisitante;
        private Integer golesVisitantePenales;
        private Integer golesLocalPenales;
        private String fecha;
        private String hora;
        private String estado;
        private String cancha;
        private String arbitro;
        private Integer orden;
    }
}
