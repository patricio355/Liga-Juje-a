package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Torneo;
import com.patricio.springboot.app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TorneoRepository extends JpaRepository<Torneo, Integer> {
    List<Torneo> findByEstadoAndIdNotIn(String estado, List<Long> ids);
    Optional<Torneo> findById(Long id);
    List<Torneo> findByEstado(String estado);
    List<Torneo> findByIdNotIn(List<Long> ids);
    List<Torneo> findByEstadoAndTipo(String estado, String tipo);

    List<Torneo> findByEstadoAndTipoAndIdNotIn(
            String estado,
            String tipo,
            List<Long> ids
    );
}
