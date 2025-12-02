package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.Usuario;
import com.patricio.springboot.app.mapper.UsuarioMapper;
import com.patricio.springboot.app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public AdminService(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario crearAdministrador(UsuarioDTO dto) {

        // validar email duplicado
        if (!usuarioService.emailDisponible(dto.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }

        // mapear DTO
        Usuario admin = UsuarioMapper.toEntity(dto);

        // establecer rol ADMIN
        admin.setRol("ADMIN");

        // guardar usuario usando lógica común
        return usuarioService.guardarUsuario(admin);
    }
}
