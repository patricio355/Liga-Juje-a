package com.patricio.springboot.app.repository;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    Optional<Equipo> findById(Long id);

    List<Equipo> findAllByEstado(boolean estado);

    @Query("SELECT e FROM Equipo e WHERE e.creador.email = :email")
    List<Equipo> findByCreadorEmail(@Param("email") String email);

    boolean existsByEncargadoAndIdNot(Usuario encargado, Long id);
    boolean existsByEncargado(Usuario encargado);

    // Para el Admin: Traer todos los que NO estén eliminados (estado = true)
    @Query("SELECT e FROM Equipo e WHERE e.estado = true")
    List<Equipo> findAllActivos();

    @Query("SELECT DISTINCT e FROM Equipo e LEFT JOIN FETCH e.creador")
    List<Equipo> findAllComplete();

    // Para el Encargado: Traer sus equipos que NO estén eliminados
    @Query("SELECT e FROM Equipo e WHERE e.creador.email = :email AND e.estado = true")
    List<Equipo> findByCreadorEmailAndEstadoTrue(@Param("email") String email);
}
