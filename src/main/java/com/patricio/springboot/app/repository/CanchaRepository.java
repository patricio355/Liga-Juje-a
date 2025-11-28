package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanchaRepository extends JpaRepository<Cancha, Integer>{
}
