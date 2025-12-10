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

        // Calcular goles usando stats del partido
        this.golesLocal = partido.getEstadisticas().stream()
                .filter(e -> e.getJugador().getEquipo().getId()
                        .equals(partido.getEquipoLocal().getId()))
                .mapToInt(e -> e.getGoles())
                .sum();

        this.golesVisitante = partido.getEstadisticas().stream()
                .filter(e -> e.getJugador().getEquipo().getId()
                        .equals(partido.getEquipoVisitante().getId()))
                .mapToInt(e -> e.getGoles())
                .sum();

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
