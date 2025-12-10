package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.EquipoZona;
import com.patricio.springboot.app.entity.Jugador;
import com.patricio.springboot.app.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface PartidoRepository extends JpaRepository<Partido, Integer> {
    // Listar partidos por zona
    List<Partido> findByZonaId(Long zonaId);

    Optional<Partido> findById(Long id);


    // Listar partidos donde participa un equipo (local o visitante)
    List<Partido> findByEquipoLocalIdOrEquipoVisitanteId(Long equipoLocalId, Long equipoVisitanteId);

    // Listar partidos pendientes (útil si querés mostrar los próximos)
    List<Partido> findByEstado(String estado);
}
