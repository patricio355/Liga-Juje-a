package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.EstadisticaJugador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadisticaJugadorRepository extends JpaRepository<EstadisticaJugador, Long> {
}
