package com.patricio.springboot.app.repository;

import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.creador WHERE u.email = :email AND u.activo = true")
    Optional<Usuario> findByEmailAndActivoTrue(@Param("email") String email);

    Optional<Usuario> findByNombre(String username);

    @Query("SELECT new com.patricio.springboot.app.dto.UsuarioDTO(u.id, u.nombre, u.email) " +
            "FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    List<UsuarioDTO> findUsuariosByRol(@Param("rol") String rol);


    // Para el Admin: Todos los usuarios activos (sin importar el creador)
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.creador")
    List<Usuario> findAllActivos();

    // Para el Encargado: Solo sus usuarios creados que estén activos
    @Query("SELECT u FROM Usuario u WHERE u.creador.id = :creadorId AND u.activo = true")
    List<Usuario> findByCreadorId(@Param("creadorId") Long creadorId);

}