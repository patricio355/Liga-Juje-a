package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidoRepository extends JpaRepository<Partido, Integer> {
}
