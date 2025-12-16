package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.entity.Encargado;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    Optional<Equipo> findById(Long id);

    List<Equipo> findAllByEstado(boolean estado);
    boolean existsByEncargadoAndIdNot(Encargado encargado, Long id);


    boolean existsByEncargado(Usuario encargado);
}
