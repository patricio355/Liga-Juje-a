package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.PartidoEliminacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidoEliminacionRepository extends JpaRepository<PartidoEliminacion, Integer> {
}
