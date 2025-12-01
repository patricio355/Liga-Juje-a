package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    Optional<Equipo> findById(Long id);

    List<Equipo> findAllByEstado(boolean estado);
}
