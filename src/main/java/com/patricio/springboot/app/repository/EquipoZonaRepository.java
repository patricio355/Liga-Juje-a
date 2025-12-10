package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.EquipoZona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipoZonaRepository extends JpaRepository<EquipoZona, Long> {

    List<EquipoZona> findByZonaId(Long zonaId);

    List<EquipoZona> findByEquipoId(Long equipoId);

    EquipoZona findByEquipoIdAndZonaId(Long equipoId, Long zonaId);



    @Query("""
       SELECT ez FROM EquipoZona ez
       WHERE ez.zona.id = :zonaId
       ORDER BY ez.puntos DESC,
                (ez.golesAFavor - ez.golesEnContra) DESC,
                ez.golesAFavor DESC
       """)
    List<EquipoZona> listarTablaPosiciones(Long zonaId);
}
