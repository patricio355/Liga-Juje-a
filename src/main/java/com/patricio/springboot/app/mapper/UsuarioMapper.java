package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.Usuario;
public class UsuarioMapper {

    public static UsuarioDTO toDTO(Usuario u) {
        return new UsuarioDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getDni(),
                u.getTelefono(),
                u.getDomicilio(),
                u.getRol(),
                u.isActivo()
        );
    }

    public static Usuario toEntity(UsuarioDTO dto) {
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setDni(dto.getDni());
        u.setTelefono(dto.getTelefono());
        u.setDomicilio(dto.getDomicilio());
        u.setRol(dto.getRol());
        u.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return u;
    }
}
