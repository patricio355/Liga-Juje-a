package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
}
