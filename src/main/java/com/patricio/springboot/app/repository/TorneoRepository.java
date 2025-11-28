package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TorneoRepository extends JpaRepository<Torneo, Integer> {
}
