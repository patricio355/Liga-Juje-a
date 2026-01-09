package com.patricio.springboot.app.mapper;

import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.Usuario;
public class UsuarioMapper {

    public static UsuarioDTO toDTO(Usuario u) {
        if (u == null) return null;
        UsuarioDTO dto = new UsuarioDTO();

        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setDni(u.getDni());
        dto.setTelefono(u.getTelefono());
        dto.setDomicilio(u.getDomicilio());
        dto.setRol(u.getRol());
        dto.setActivo(u.isActivo());

        if (u.getCreador() != null) {
            dto.setCreadorId(u.getCreador().getId());
            dto.setCreadorNombre(u.getCreador().getNombre());
        }

        return dto;
    }

    public static Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) return null;

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

    public static void updateEntity(Usuario u, UsuarioDTO dto) {
        if (dto == null || u == null) return;

        u.setNombre(dto.getNombre());
        u.setDni(dto.getDni());
        u.setTelefono(dto.getTelefono());
        u.setDomicilio(dto.getDomicilio());
        u.setActivo(dto.getActivo() != null ? dto.getActivo() : u.isActivo());

    }
}
