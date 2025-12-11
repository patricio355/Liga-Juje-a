package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZonaRepository extends JpaRepository<Zona, Integer> {
    Optional<Zona> findById(Long id);


}
