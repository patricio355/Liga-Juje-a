package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.EtapaTorneo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformeRepository extends JpaRepository<EtapaTorneo, Integer> {
}
