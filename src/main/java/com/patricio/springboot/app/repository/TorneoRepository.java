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

    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.encargado LEFT JOIN FETCH t.zonas WHERE t.id = :id")
    Optional<Torneo> findByIdOptimized(@Param("id") Long id);
    // Usamos JOIN FETCH para traer las zonas y evitar el problema N+1
    @Query("SELECT DISTINCT t FROM Torneo t " +
            "LEFT JOIN FETCH t.zonas z " +
            "WHERE t.estado = :estado")
    List<Torneo> findByEstadoConZonas(@Param("estado") String estado);


    List<Torneo> findByIdNotIn(List<Long> ids);
    List<Torneo> findByEstadoAndTipo(String estado, String tipo);

    List<Torneo> findByEstadoAndTipoAndIdNotIn(
            String estado,
            String tipo,
            List<Long> ids
    );

    List<Torneo> findByEncargadoEmail(String email);


    @Query("SELECT DISTINCT t FROM Torneo t LEFT JOIN FETCH t.zonas")
    List<Torneo> findAllWithZonas();

    @Query("SELECT DISTINCT t FROM Torneo t LEFT JOIN FETCH t.zonas WHERE t.encargado.email = :email")
    List<Torneo> findByEncargadoEmailWithZonas(@Param("email") String email);
}
