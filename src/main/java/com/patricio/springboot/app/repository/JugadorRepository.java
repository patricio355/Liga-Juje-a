package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {

    Optional<Jugador> findById(Long id);

    boolean existsByDni(int dni);

    List<Jugador> findByEquipoId(Long equipoId);
}
