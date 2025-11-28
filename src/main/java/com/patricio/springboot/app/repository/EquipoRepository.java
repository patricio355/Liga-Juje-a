package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
}
