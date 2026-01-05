package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    Optional<Zona> findById(Long id);
    List<Zona> findByTorneoId(Long torneoId);
    @Query("SELECT z FROM Zona z WHERE z.torneo.id = :torneoId")
    List<Zona> findByTorneoIdOptimized(@Param("torneoId") Long torneoId);

    @Query("SELECT z FROM Zona z WHERE z.id = :id")
    Optional<Zona> findByIdOptimized(@Param("id") Long id);
}
