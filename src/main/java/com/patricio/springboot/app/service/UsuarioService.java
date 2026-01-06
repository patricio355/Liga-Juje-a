package com.patricio.springboot.app.service;
import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.Usuario;
import com.patricio.springboot.app.mapper.UsuarioMapper;
import com.patricio.springboot.app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }


    private static final List<String> ROLES_VALIDOS = List.of(
            "ADMIN",
            "ENCARGADOEQUIPO",
            "ENCARGADOTORNEO",
            "ARBITRO",
            "VEEDOR"
    );

    private void validarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw new RuntimeException("El rol es obligatorio");
        }

        if (!ROLES_VALIDOS.contains(rol)) {
            throw new RuntimeException("Rol inválido");
        }
    }

    // =========================
    // LISTAR TODOS
    // =========================
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::toDTO)
                .toList();
    }

    // =========================
    // CREAR USUARIO (ADMIN)
    // =========================
    public UsuarioDTO crearUsuarioAdmin(UsuarioDTO dto, String password) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }


        validarEmail(dto.getEmail());
        validarRol(dto.getRol());
        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setContrasenia(passwordEncoder.encode(password));
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        return UsuarioMapper.toDTO(guardado);
    }




    private void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("El email es obligatorio");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Formato de email inválido");
        }
    }
    // =========================
    // EDITAR USUARIO
    // =========================
    public UsuarioDTO editarUsuario(Long id, UsuarioDTO dto) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        // Email → validar si cambia
        if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("El email ya está en uso");
            }
            usuario.setEmail(dto.getEmail());
        }

        validarEmail(dto.getEmail());
        validarRol(dto.getRol());
        usuario.setNombre(dto.getNombre());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDomicilio(dto.getDomicilio());
        usuario.setDni(dto.getDni());
        usuario.setRol(dto.getRol());

        return UsuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    // =========================
    // BAJA LÓGICA
    // =========================
    public void desactivarUsuario(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }


    public List<UsuarioDTO> listarArbitros() {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> "ARBITRO".equals(u.getRol()) && u.isActivo()) // Filtra por rol y que esté activo
                .map(UsuarioMapper::toDTO)
                .toList();
    }
}
