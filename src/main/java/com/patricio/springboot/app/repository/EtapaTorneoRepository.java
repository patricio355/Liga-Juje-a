package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.EtapaTorneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtapaTorneoRepository extends JpaRepository<EtapaTorneo, Long> {
    Optional<EtapaTorneo> findById(Long id);

    Optional<EtapaTorneo> findByTorneoIdAndNombre(Long torneoId, String nombre);
    List<EtapaTorneo> findByTorneoIdOrderByOrdenAsc(Long torneoId);
}
