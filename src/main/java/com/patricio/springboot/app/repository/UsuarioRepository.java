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

    Optional<Usuario> findByNombre(String username);

    @Query("SELECT new com.patricio.springboot.app.dto.UsuarioDTO(u.id, u.nombre, u.email) " +
            "FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    List<UsuarioDTO> findUsuariosByRol(@Param("rol") String rol);
}