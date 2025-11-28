package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaRepository extends JpaRepository<Zona, Integer> {
}
