package com.patricio.springboot.app.service;
import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.UsuarioMapper;
import com.patricio.springboot.app.repository.UsuarioRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    @Transactional(readOnly = true)
    @Cacheable(value = "usuariosList", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")// Cacheamos el resultado para mejorar velocidad
    public List<UsuarioDTO> listarUsuarios() {
        // 1. Obtener el usuario que hace la petición
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario logueado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String rol = logueado.getRol().toUpperCase();
        List<Usuario> lista;

        // 2. Aplicar lógica de filtrado según rol
        if (rol.contains("ADMIN")) {
            // El admin ve el catálogo completo de activos
            lista = usuarioRepository.findAllActivos();
        } else {
            // El encargado solo ve los árbitros/veedores/etc que él creó
            lista = usuarioRepository.findByCreadorId(logueado.getId());
        }

        // 3. Convertir a DTO usando el Mapper (que ya trae el nombre del creador)
        return lista.stream()
                .map(UsuarioMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "usuariosEncargados")
    public List<UsuarioDTO> listarEncargadosOptimizado() {
        // Usamos el rol que definiste en tu lógica anterior
        return usuarioRepository.findUsuariosByRol("ENCARGADOTORNEO");
    }

    @Transactional
    @CacheEvict(value = "usuariosList", allEntries = true)
    public UsuarioDTO crearUsuarioAdmin(UsuarioDTO dto, String password) {
        // 1. Obtener el Creador desde la sesión
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogueado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Error de sesión: Usuario no encontrado"));

        String rolLogueado = usuarioLogueado.getRol().toUpperCase();
        String rolNuevo = dto.getRol().toUpperCase();

        // 2. Restricciones de seguridad de Roles
        if (!rolLogueado.contains("ADMIN")) {
            List<String> rolesPermitidos = List.of("ENCARGADOEQUIPO", "ARBITRO", "VEEDOR");
            if (!rolesPermitidos.contains(rolNuevo)) {
                throw new RuntimeException("No tienes permisos para crear el rol: " + rolNuevo);
            }
        }

        // 3. Limpieza y validación de Email con Lógica de Reactivación
        String emailLimpio = dto.getEmail().trim().toLowerCase();
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(emailLimpio);

        if (usuarioExistente.isPresent()) {
            Usuario u = usuarioExistente.get();

            // Si ya está activo, lanzamos el error normal
            if (u.isActivo()) {
                throw new RuntimeException("El email " + emailLimpio + " ya está registrado.");
            }

            // --- LÓGICA DE REACTIVACIÓN AUTOMÁTICA ---
            // Si llegamos aquí es porque el usuario existe pero está inactivo.
            // Actualizamos sus datos con los del DTO (nombre, dni, tel, domicilio, etc.)
            UsuarioMapper.updateEntity(u, dto);

            // Actualizamos contraseña, estado y quién lo reactiva
            u.setContrasenia(passwordEncoder.encode(password));
            u.setActivo(true);
            u.setCreador(usuarioLogueado);

            // Importante: No cambiamos la instancia (new), solo guardamos la que ya existía
            try {
                Usuario reactivado = usuarioRepository.save(u);
                return UsuarioMapper.toDTO(reactivado);
            } catch (Exception e) {
                throw new RuntimeException("Error al reactivar la cuenta existente.");
            }
        }

        // 4. Instanciación según Herencia (Para nuevos registros)
        Usuario usuario;
        if (rolNuevo.contains("ADMIN")) {
            usuario = new Administrador();
        } else if (rolNuevo.contains("ENCARGADOTORNEO")) {
            usuario = new EncargadoTorneo();
        } else if (rolNuevo.contains("ENCARGADOEQUIPO")) {
            usuario = new EncargadoEquipo();
        } else if (rolNuevo.contains("ARBITRO")) {
            usuario = new Arbitro();
        } else if (rolNuevo.contains("VEEDOR")) {
            usuario = new Veedor();
        } else {
            usuario = new Usuario();
        }

        // 5. ¡USO DEL MAPPER!
        UsuarioMapper.updateEntity(usuario, dto);

        // 6. Seteos manuales de seguridad y relación
        usuario.setEmail(emailLimpio);
        usuario.setContrasenia(passwordEncoder.encode(password));
        usuario.setActivo(true);
        usuario.setCreador(usuarioLogueado);

        // 7. Persistencia de nuevo usuario
        try {
            Usuario guardado = usuarioRepository.save(usuario);
            return UsuarioMapper.toDTO(guardado);
        } catch (Exception e) {
            System.err.println("ERROR REAL AL GUARDAR: " + e.getMessage());
            throw new RuntimeException("Error en base de datos al persistir el usuario.");
        }
    }




    private void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("El email es obligatorio");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Formato de email inválido");
        }
    }

    @Transactional
    @CacheEvict(value = "usuariosList", allEntries = true)
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
    @Transactional
    @CacheEvict(value = "usuariosList", allEntries = true)
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
