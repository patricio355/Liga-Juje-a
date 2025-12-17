package com.patricio.springboot.app.dto;

import com.patricio.springboot.app.entity.Partido;
import lombok.Data;

@Data
public class ResultadoPartidoResponse {

    private Long partidoId;
    private String equipoLocal;
    private String equipoVisitante;
    private int golesLocal;
    private int golesVisitante;
    private String estado;
    private String ganador;

    public ResultadoPartidoResponse(Partido partido) {

        this.partidoId = partido.getId();
        this.equipoLocal = partido.getEquipoLocal().getNombre();
        this.equipoVisitante = partido.getEquipoVisitante().getNombre();

        this.golesLocal = partido.getGolesLocal();
        this.golesVisitante = partido.getGolesVisitante();

        this.estado = partido.getEstado();

        if (golesLocal > golesVisitante) {
            this.ganador = equipoLocal;
        } else if (golesVisitante > golesLocal) {
            this.ganador = equipoVisitante;
        } else {
            this.ganador = "Empate";
        }
    }
}
