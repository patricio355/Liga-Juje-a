package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.EquipoZona;
import com.patricio.springboot.app.entity.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipoZonaRepository extends JpaRepository<EquipoZona, Long> {



    // Para inscribir
    boolean existsByNombreEquipoIgnoreCaseAndZona_Torneo_Id(
            String nombreEquipo,
            Long torneoId
    );

    // Para editar
    boolean existsByNombreEquipoIgnoreCaseAndZona_Torneo_IdAndEquipo_IdNot(
            String nombreEquipo,
            Long torneoId,
            Long equipoId
    );


    List<EquipoZona> findByEquipoId(Long equipoId);

    EquipoZona findByEquipoIdAndZonaId(Long equipoId, Long zonaId);

    @Query("""
        SELECT DISTINCT ez.torneoId
        FROM EquipoZona ez
        WHERE ez.equipo.id = :equipoId
    """)
    List<Long> findTorneoIdsByEquipoId(Long equipoId);

    @Query("""
       SELECT ez FROM EquipoZona ez
       WHERE ez.zona.id = :zonaId
       ORDER BY ez.puntos DESC,
                (ez.golesAFavor - ez.golesEnContra) DESC,
                ez.golesAFavor DESC
       """)
    List<EquipoZona> listarTablaPosiciones(Long zonaId);
}
