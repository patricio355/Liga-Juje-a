package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.Usuario;
import com.patricio.springboot.app.mapper.UsuarioMapper;
import com.patricio.springboot.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UsuarioService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario guardarUsuario(Usuario usuario){
        usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
        return usuarioRepository.save(usuario);
    }

    public boolean emailDisponible(String email){
        return usuarioRepository.findByEmail(email).isEmpty();
    }
}
