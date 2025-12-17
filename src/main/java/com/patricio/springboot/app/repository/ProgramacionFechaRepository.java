package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.ProgramacionFecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProgramacionFechaRepository
        extends JpaRepository<ProgramacionFecha, Long> {

    List<ProgramacionFecha> findByZonaIdAndNumeroFecha(
            Long zonaId,
            Integer numeroFecha
    );

    boolean existsByZonaIdAndNumeroFechaAndPartidoId(
            Long zonaId,
            Integer numeroFecha,
            Long partidoId
    );

    


    @Query("""
        SELECT COUNT(pf) > 0
        FROM ProgramacionFecha pf
        WHERE pf.zona.id = :zonaId
          AND pf.numeroFecha = :fecha
          AND (
              pf.partido.equipoLocal.id = :equipoId
           OR pf.partido.equipoVisitante.id = :equipoId
          )
    """)
    boolean equipoYaProgramadoEnFecha(
            @Param("zonaId") Long zonaId,
            @Param("fecha") Integer fecha,
            @Param("equipoId") Long equipoId
    );


    @Query("""
    select count(pf) > 0
    from ProgramacionFecha pf
    where pf.zona.id = :zonaId
      and pf.partido.id = :partidoId
""")
    boolean existePartidoEnZona(
            @Param("zonaId") Long zonaId,
            @Param("partidoId") Long partidoId
    );

    @Query("""
    select pf.partido.id
    from ProgramacionFecha pf
    where pf.zona.id = :zonaId
""")
    List<Long> findPartidoIdsByZona(@Param("zonaId") Long zonaId);


    @Query("""
    select case
        when p.equipoLocal.id = :equipoId then p.equipoVisitante.id
        else p.equipoLocal.id
    end
    from ProgramacionFecha pf
    join pf.partido p
    where pf.zona.id = :zonaId
      and pf.numeroFecha = :fecha
      and (p.equipoLocal.id = :equipoId or p.equipoVisitante.id = :equipoId)
""")
    List<Long> findEquiposRivalesEnFecha(
            @Param("zonaId") Long zonaId,
            @Param("fecha") Integer fecha,
            @Param("equipoId") Long equipoId
    );


    List<ProgramacionFecha> findByZonaId(Long zonaId);
}
