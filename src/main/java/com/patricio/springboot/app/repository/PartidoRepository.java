package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.EquipoZona;
import com.patricio.springboot.app.entity.Jugador;
import com.patricio.springboot.app.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
    // Listar partidos por zona
    List<Partido> findByZonaId(Long zonaId);

    Optional<Partido> findById(Long id);

    List<Partido> findByZonaIdOrderByNumeroFechaAscFechaHoraAsc(Long idZona);

    boolean existsByZonaIdAndEquipoLocalIdAndEquipoVisitanteId(
            Long zonaId, Long equipoLocalId, Long equipoVisitanteId);


    @Query("SELECT COALESCE(MAX(p.numeroFecha), 0) FROM Partido p WHERE p.zona.id = :zonaId")
    int obtenerUltimaFecha(@Param("zonaId") Long zonaId);

    @Query("""
SELECT COUNT(p) > 0 FROM Partido p
WHERE p.zona.id = :zonaId
AND (
   (p.equipoLocal.id = :e1 AND p.equipoVisitante.id = :e2)
OR (p.equipoLocal.id = :e2 AND p.equipoVisitante.id = :e1)
)
""")
    boolean existsByZonaIdAndEquipos(@Param("zonaId") Long zonaId,
                                     @Param("e1") Long equipo1,
                                     @Param("e2") Long equipo2);

    // Listar partidos donde participa un equipo (local o visitante)
    List<Partido> findByEquipoLocalIdOrEquipoVisitanteId(Long equipoLocalId, Long equipoVisitanteId);

    // Listar partidos pendientes (útil si querés mostrar los próximos)
    List<Partido> findByEstado(String estado);


    @Query("""
    SELECT COUNT(p) > 0
    FROM Partido p
    WHERE p.zona.id = :zonaId
      AND p.numeroFecha = :fecha
      AND (p.equipoLocal.id = :equipoId OR p.equipoVisitante.id = :equipoId)
""")
    boolean existsByZonaIdAndEquipoAndFecha(
            @Param("zonaId") Long zonaId,
            @Param("equipoId") Long equipoId,
            @Param("fecha") Integer fecha
    );

    boolean existsByZonaId(Long zonaId);

    @Query("""
SELECT COUNT(p) > 0
FROM Partido p
WHERE p.zona.id = :zonaId
  AND p.estado = 'FINALIZADO'
  AND (
       (p.equipoLocal.id = :a AND p.equipoVisitante.id = :b)
    OR (p.equipoLocal.id = :b AND p.equipoVisitante.id = :a)
  )
""")
    boolean existsFinalizadoEntre(
            @Param("zonaId") Long zonaId,
            @Param("a") Long a,
            @Param("b") Long b
    );

    List<Partido> findByZonaIdAndEstado(Long zonaId, String finalizado);

    void deleteByZonaIdAndEstado(Long zonaId, String pendiente);

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

}
