package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.EstadisticaJugadorDTO;
import com.patricio.springboot.app.entity.EstadisticaJugador;
import com.patricio.springboot.app.entity.Jugador;
import com.patricio.springboot.app.entity.Partido;
import com.patricio.springboot.app.repository.EstadisticaJugadorRepository;
import com.patricio.springboot.app.repository.JugadorRepository;
import com.patricio.springboot.app.repository.PartidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstadisticaJugadorService {

    private final EstadisticaJugadorRepository estadisticaJugadorRepository;
    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;

    public EstadisticaJugador registrarEstadistica(EstadisticaJugadorDTO dto) {

        Partido partido = partidoRepository.findById(dto.getPartidoId())
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        Jugador jugador = jugadorRepository.findById(dto.getJugadorId())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // Validar que el jugador pertenece al partido
        if (!jugador.getEquipo().getId().equals(partido.getEquipoLocal().getId()) &&
                !jugador.getEquipo().getId().equals(partido.getEquipoVisitante().getId())) {
            throw new RuntimeException("El jugador no pertenece a este partido");
        }

        EstadisticaJugador estad = new EstadisticaJugador();
        estad.setPartido(partido);
        estad.setJugador(jugador);
        estad.setGoles(dto.getGoles());
        estad.setAmarillas(dto.getAmarillas());
        estad.setRojas(dto.getRojas());

        return estadisticaJugadorRepository.save(estad);
    }
}