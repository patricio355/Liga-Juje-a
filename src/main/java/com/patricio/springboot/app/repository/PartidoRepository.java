
package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    List<Partido> findByZonaId(Long zonaId);

    Optional<Partido> findById(Long id);

    // CORRECCIÓN: Cambiamos FechaHoraAsc por FechaAscHoraAsc
    List<Partido> findByZonaIdOrderByNumeroFechaAscFechaAscHoraAsc(Long idZona);

    boolean existsByZonaId(Long zonaId);

    @Query("""
    select count(p) > 0
    from Partido p
    where p.zona.id = :zonaId
      and (
        (p.equipoLocal.id = :a and p.equipoVisitante.id = :b)
        or
        (p.equipoLocal.id = :b and p.equipoVisitante.id = :a)
      )
    """)
    boolean existsEntreEquipos(Long zonaId, Long a, Long b);

    @Query("select max(p.numeroFecha) from Partido p where p.zona.id = :zonaId")
    Optional<Integer> findMaxNumeroFechaByZonaId(Long zonaId);

    @Query("SELECT DISTINCT p.numeroFecha FROM Partido p " +
            "WHERE p.zona.id = :zonaId " +
            "AND p.equipoLocal IS NOT NULL " +
            "AND p.equipoVisitante IS NOT NULL " +
            "ORDER BY p.numeroFecha ASC")
    List<Integer> findDistinctNumeroFechaByZonaId(@Param("zonaId") Long zonaId);

    void deleteByZonaId(Long idZona);

    @Query("SELECT p FROM Partido p WHERE p.zona.id = :zonaId AND (p.equipoLocal.id = :equipoId OR p.equipoVisitante.id = :equipoId)")
    List<Partido> findAllByZonaIdAndEquipoId(Long zonaId, Long equipoId);
}