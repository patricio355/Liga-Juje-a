package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {

    Jugador getById(Long id);
}
