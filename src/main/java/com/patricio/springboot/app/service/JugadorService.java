package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.JugadorDTO;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.Jugador;
import com.patricio.springboot.app.mapper.JugadorMapper;
import com.patricio.springboot.app.repository.EquipoRepository;
import com.patricio.springboot.app.repository.JugadorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class JugadorService {

    private JugadorRepository jugadorRepository;
    private EquipoRepository equipoRepository;
    public JugadorService(JugadorRepository jugadorRepository, EquipoRepository equipoRepository) {
        this.jugadorRepository = jugadorRepository;
        this.equipoRepository = equipoRepository;
    }





    public Jugador obtenerJugadorID(Long id) {
        return jugadorRepository.findById(id).orElseThrow( () -> new RuntimeException("jugador no encontrado"));
    }

    public JugadorDTO crearJugador(JugadorDTO dto) {

        // Validación por DNI
        if (jugadorRepository.existsByDni((dto.getDni()))) {
            throw new RuntimeException("Ya existe un jugador con ese DNI");
        }

        Jugador jugador = JugadorMapper.toEntity(dto);

        // Si el DTO trae idEquipo, lo asignamos
        if (dto.getIdEquipo() != null) {
            Equipo equipo = equipoRepository.findById(dto.getIdEquipo())
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
            jugador.setEquipo(equipo);
        }



        Jugador guardado = jugadorRepository.save(jugador);

        return JugadorMapper.toDTO(guardado);
    }

}
