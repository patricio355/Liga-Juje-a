package com.patricio.springboot.app.service;

import com.patricio.springboot.app.entity.Jugador;
import com.patricio.springboot.app.repository.EquipoRepository;
import com.patricio.springboot.app.repository.JugadorRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JugadorService {

    private JugadorRepository jugadorRepository;
    public JugadorService(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }





    public Jugador obtenerJugadorID(Long id) {
        return jugadorRepository.getById(id);
    }
}
