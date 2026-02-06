package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Torneo;
import com.patricio.springboot.app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TorneoRepository extends JpaRepository<Torneo, Integer> {

    Optional<Torneo> findById(Long id);


    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.encargado LEFT JOIN FETCH t.zonas WHERE t.id = :id")
    Optional<Torneo> findByIdOptimized(@Param("id") Long id);

    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.zonas LEFT JOIN FETCH t.encargado WHERE t.slug = :slug")
    Optional<Torneo> findBySlugOptimized(@Param("slug") String slug);

    @Query("SELECT DISTINCT t FROM Torneo t " +
            "LEFT JOIN FETCH t.zonas z " +
            "WHERE t.estado = :estado")
    List<Torneo> findByEstadoConZonas(@Param("estado") String estado);

    // 1. Busca cuando SÍ hay una división específica (Ej: "Torneo Apertura" + Div "A")
    boolean existsByNombreIgnoreCaseAndDivisionAndEstadoIgnoreCase(String nombre, String division, String estado);

    // 2. Busca cuando NO hay división (Ej: "Torneo Apertura" + null)
    boolean existsByNombreIgnoreCaseAndDivisionIsNullAndEstadoIgnoreCase(String nombre, String estado);

    // 1. Para Modificar: Mismo Nombre + Misma División + Activo + DISTINTO ID
    boolean existsByNombreIgnoreCaseAndDivisionAndEstadoIgnoreCaseAndIdNot(
            String nombre,
            String division,
            String estado,
            Long id
    );

    // 2. Para Modificar: Mismo Nombre + División NULL + Activo + DISTINTO ID
    boolean existsByNombreIgnoreCaseAndDivisionIsNullAndEstadoIgnoreCaseAndIdNot(
            String nombre,
            String estado,
            Long id
    );

    List<Torneo> findByEstadoAndTipo(String estado, String tipo);

    List<Torneo> findByEstadoAndTipoAndIdNotIn(
            String estado,
            String tipo,
            List<Long> ids
    );

    @Query("SELECT DISTINCT t FROM Torneo t LEFT JOIN FETCH t.zonas")
    List<Torneo> findAllWithZonas();

    @Query("SELECT DISTINCT t FROM Torneo t " +
            "LEFT JOIN FETCH t.zonas " +
            "WHERE t.encargado.email = :email")
    List<Torneo> findByEncargadoEmailWithZonas(@Param("email") String email);

    boolean existsBySlug(String slugFinal);

    List<Torneo> findByEstadoIgnoreCase(String estado);


}
