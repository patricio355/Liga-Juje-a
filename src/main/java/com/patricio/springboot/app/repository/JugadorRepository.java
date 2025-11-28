package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {
}
