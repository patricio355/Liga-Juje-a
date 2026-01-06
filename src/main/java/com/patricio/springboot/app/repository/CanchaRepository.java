package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CanchaRepository extends JpaRepository<Cancha, Integer>{
     Optional<Cancha> findById(Long id);

    List<Cancha> findAllByEstadoTrue();

    Optional<Cancha> findByNombre(String canchaNombre);
}
