package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Partido;
import com.patricio.springboot.app.entity.SolicitudCierrePartido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudCierrePartidoRepository
        extends JpaRepository<SolicitudCierrePartido, Long> {

    boolean existsByPartidoAndEstado(
            Partido partido,
            String estado
    );

    List<SolicitudCierrePartido> findByEstado(String estado);

    // opcional pero muy útil
    List<SolicitudCierrePartido> findByPartidoAndEstado(
            Partido partido,
            String estado
    );
}
