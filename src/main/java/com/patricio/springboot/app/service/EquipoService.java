package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.*;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.EquipoZonaMapper;
import com.patricio.springboot.app.mapper.JugadorMapper;
import com.patricio.springboot.app.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.patricio.springboot.app.mapper.EquipoMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class EquipoService {
    private EquipoRepository equipoRepository;
    private ZonaRepository zonaRepository;
    private CanchaRepository canchaRepository;
    private JugadorRepository jugadorRepository;
    private EquipoZonaRepository equipoZonaRepository;
    private UsuarioRepository usuarioRepository;
    private PartidoService partidoService;


    public EquipoService(EquipoRepository equipoRepository, UsuarioRepository usuarioRepository,PartidoService partidoService , ZonaRepository zonaRepository, CanchaRepository canchaRepository , JugadorRepository jugadorRepository, EquipoZonaRepository equipoZonaRepository) {
        this.equipoRepository = equipoRepository;
        this.zonaRepository = zonaRepository;
        this.canchaRepository = canchaRepository;
        this.jugadorRepository = jugadorRepository;
        this.equipoZonaRepository = equipoZonaRepository;
        this.usuarioRepository = usuarioRepository;
        this.partidoService = partidoService;

    }

    public List<EquipoDTO> listarEquipos() {

        return equipoRepository.findAll()
                .stream()
                .map(equipo -> {

                    // 1️⃣ Equipo básico
                    EquipoDTO dto = EquipoMapper.toDTO(equipo);

                    // 2️⃣ Buscar inscripciones (equipo_zona)
                    List<EquipoZonaDTO> inscripciones =
                            equipoZonaRepository
                                    .findByEquipoId(equipo.getId())
                                    .stream()
                                    .map(EquipoZonaMapper::toDTO)
                                    .toList();

                    // 3️⃣ Setearlas en el DTO
                    dto.setInscripciones(inscripciones);

                    return dto;
                })
                .toList();
    }

    public List<EquipoDTO> listarEquiposActivos() {

        return equipoRepository.findAllByEstado(true)
                .stream()
                .map(equipo -> {

                    EquipoDTO dto = EquipoMapper.toDTO(equipo);

                    List<EquipoZonaDTO> inscripciones =
                            equipoZonaRepository
                                    .findByEquipoId(equipo.getId())
                                    .stream()
                                    .map(EquipoZonaMapper::toDTO)
                                    .toList();

                    dto.setInscripciones(inscripciones);

                    return dto;
                })
                .toList();
    }

    public List<EquipoDTO> listarEquiposSegunRol(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        boolean esAdmin = usuario.getRol().equalsIgnoreCase("ROLE_ADMIN") ||
                usuario.getRol().equalsIgnoreCase("ADMIN");

        if (esAdmin) {
            // Usamos el nuevo método que filtra por estado = true
            return equipoRepository.findAllComplete().stream()
                    .map(EquipoMapper::toDTO)
                    .toList();
        }

        // Usamos el nuevo método filtrado para encargados
        return equipoRepository.findByCreadorEmailAndEstadoTrue(email).stream()
                .map(EquipoMapper::toDTO)
                .toList();
    }

    public List<JugadorDTO> listarJugadores(Long idEquipo) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        return jugadorRepository.findByEquipoId(idEquipo)
                .stream()
                .map(JugadorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "programacionData", allEntries = true),
            @CacheEvict(value = "misEquipos", allEntries = true) // Agregado para refrescar lista de equipos
    })
    public EquipoDTO crearEquipo(EquipoDTO dto) {
        // 1. Convertir DTO a Entidad y asegurar datos básicos
        Equipo equipo = EquipoMapper.toEntity(dto);
        equipo.setFechaCreacion(LocalDate.now());
        equipo.setEstado(true); // Evita error 1364 de MySQL

        // 2. Asignación de Cancha
        if (dto.getCanchaId() != null) {
            Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                    .orElseThrow(() -> new RuntimeException("Cancha inexistente"));
            equipo.setLocalia(cancha);
        }

        // 3. Validación de Encargado (El que gestiona el día a día del equipo)
        if (dto.getEncargadoEmail() != null && !dto.getEncargadoEmail().trim().isEmpty()) {
            Usuario encargado = usuarioRepository.findByEmail(dto.getEncargadoEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario encargado no existe"));

            if (!encargado.getRol().contains("ENCARGADOEQUIPO")) {
                throw new RuntimeException("Este usuario no tiene el rol de encargado de equipo");
            }
            if (equipoRepository.existsByEncargado(encargado)) {
                throw new RuntimeException("Este usuario ya gestiona otro equipo");
            }
            equipo.setEncargado(encargado);
        }

        // 4. LÓGICA DE DUEÑO (CREADOR)
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogueado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Sesión no válida"));

        String rol = usuarioLogueado.getRol().toUpperCase();

        // Si es ADMIN o ENCARGADOTORNEO, pueden asignar el equipo a otra persona
        if (rol.contains("ADMIN") || rol.contains("ENCARGADOTORNEO")) {
            if (dto.getCreadorEmail() != null && !dto.getCreadorEmail().trim().isEmpty()) {
                Usuario dueñoAsignado = usuarioRepository.findByEmail(dto.getCreadorEmail())
                        .orElseThrow(() -> new RuntimeException("El usuario asignado como creador no existe"));
                equipo.setCreador(dueñoAsignado);
            } else {
                equipo.setCreador(usuarioLogueado);
            }
        } else {
            // Si es un usuario común, él es el creador/dueño obligado
            equipo.setCreador(usuarioLogueado);
        }

        Equipo guardado = equipoRepository.save(equipo);
        if (guardado.getCreador() == null) {
            throw new RuntimeException("ERROR CRÍTICO: El equipo se iba a guardar sin creador. Transacción abortada.");
        }
        System.out.println("EQUIPO GUARDADO CON ÉXITO - ID: " + guardado.getId() + " - CREADOR: " + guardado.getCreador().getEmail());
        return EquipoMapper.toDTO(guardado);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "programacionData", allEntries = true),
            @CacheEvict(value = "misEquipos", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true)
    })
    public EquipoDTO crearEquipoEnZona(EquipoDTO dto, Long zonaId) {
        // 1. Datos de la Zona y Torneo
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));
        Torneo torneo = zona.getTorneo();

        // 2. Creación del Equipo
        Equipo equipo = EquipoMapper.toEntity(dto);
        equipo.setFechaCreacion(LocalDate.now());
        equipo.setEstado(true); // Importante: Asegura el estado activo

        if (dto.getCanchaId() != null) {
            Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                    .orElseThrow(() -> new RuntimeException("Cancha inexistente"));
            equipo.setLocalia(cancha);
        }

        // Validación de Encargado de Equipo
        if (dto.getEncargadoEmail() != null && !dto.getEncargadoEmail().trim().isEmpty()) {
            Usuario encargado = usuarioRepository.findByEmail(dto.getEncargadoEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario encargado no existe"));

            if (!encargado.getRol().contains("ENCARGADOEQUIPO")) {
                throw new RuntimeException("Este usuario no es un encargado de equipo");
            }
            if (equipoRepository.existsByEncargado(encargado)) {
                throw new RuntimeException("Este usuario ya tiene un equipo asignado");
            }
            equipo.setEncargado(encargado);
        }

        // 3. ASIGNACIÓN DE CREADOR (DUEÑO)
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogueado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario logueado no encontrado"));

        String rol = usuarioLogueado.getRol().toUpperCase();

        if (rol.contains("ADMIN") || rol.contains("ENCARGADOTORNEO")) {
            if (dto.getCreadorEmail() != null && !dto.getCreadorEmail().trim().isEmpty()) {
                Usuario dueñoAsignado = usuarioRepository.findByEmail(dto.getCreadorEmail())
                        .orElseThrow(() -> new RuntimeException("Creador asignado no existe"));
                equipo.setCreador(dueñoAsignado);
            } else {
                equipo.setCreador(usuarioLogueado);
            }
        } else {
            equipo.setCreador(usuarioLogueado);
        }

        Equipo equipoGuardado = equipoRepository.save(equipo);
        if (equipoGuardado.getCreador() == null) {
            throw new RuntimeException("ERROR CRÍTICO: El equipo se iba a guardar sin creador. Transacción abortada.");
        }

        // 4. VINCULACIÓN Y ESTADÍSTICAS
        EquipoZona equipoZona = new EquipoZona();
        equipoZona.setEquipo(equipoGuardado);
        equipoZona.setNombreEquipo(equipoGuardado.getNombre());
        equipoZona.setZona(zona);
        equipoZona.setTorneoId(torneo.getId());

        // Inicialización explícita de puntos y goles
        equipoZona.setPuntos(0);
        equipoZona.setPartidosJugados(0);
        equipoZona.setGanados(0);
        equipoZona.setEmpatados(0);
        equipoZona.setPerdidos(0);
        equipoZona.setGolesAFavor(0);
        equipoZona.setGolesEnContra(0);

        equipoZonaRepository.save(equipoZona);

        // 5. REGENERAR FIXTURE SI APLICA
        if ("ABIERTO".equalsIgnoreCase(torneo.getTipo())) {
            partidoService.regenerarFixtureZona(zonaId);
        }


        return EquipoMapper.toDTO(equipoGuardado);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true)
    })
    public EquipoDTO eliminarEquipo(Long id) {
        Equipo eq = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no existe"));

        eq.setEstado(false);
        equipoRepository.save(eq);
        return EquipoMapper.toDTO(eq);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "zonasPorTorneo", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true)
    })
    public EquipoDTO editarEquipo(Long id, EquipoDTO dto) {

        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // ============================
        // VALIDAR NOMBRE POR TORNEO
        // ============================
        List<Long> torneoIds =
                equipoZonaRepository.findTorneoIdsByEquipoId(id);

        for (Long torneoId : torneoIds) {
            if (equipoZonaRepository
                    .existsByNombreEquipoIgnoreCaseAndZona_Torneo_IdAndEquipo_IdNot(
                            dto.getNombre(),
                            torneoId,
                            id
                    )) {
                throw new RuntimeException(
                        "Ya existe un equipo con ese nombre en algun torneo perteneciente"
                );
            }
        }

        // ============================
        // MANEJO DEL ENCARGADO
        // ============================
        String email = dto.getEncargadoEmail();

        if (email == null || email.isBlank()) {

            equipo.setEncargado(null);

        } else {

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("No existe un usuario con ese email"));

            if (!"ENCARGADOEQUIPO".equals(usuario.getRol())) {
                throw new RuntimeException("El usuario no es un encargado");
            }

            boolean yaAsignado = equipoRepository
                    .existsByEncargadoAndIdNot(usuario, id);

            if (yaAsignado) {
                throw new RuntimeException("Este encargado ya está asignado a otro equipo");
            }

            equipo.setEncargado(usuario);
        }

        // ============================
        // RESTO DE CAMPOS
        // ============================
        equipo.setNombre(dto.getNombre());
        equipo.setLocalidad(dto.getLocalidad());
        equipo.setEscudo(dto.getEscudo());
        // En tu Service de Java antes de guardar:
        if (dto.getEstado() == null) {
            // Si viene null, mantenemos el que ya tiene la base de datos
            equipo.setEstado(equipo.isEstado());
        } else {
            equipo.setEstado(dto.getEstado());
        }
        equipo.setCamisetaSuplente(dto.getCamisetaSuplente());
        equipo.setCamisetaTitular(dto.getCamisetaTitular());
        if (dto.getCanchaId() != null) {
            // Buscar la entidad Cancha real en la base de datos
            Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                    .orElseThrow(() -> new RuntimeException("Cancha no encontrada con ID: " + dto.getCanchaId()));

            // Asignar el objeto Cancha completo al Equipo
            equipo.setLocalia(cancha);
        } else {
            // Si es opcional, puedes setearlo como null
            equipo.setLocalia(null);
        }
        // 💾 Guardar equipo
        equipoRepository.save(equipo);

        // ============================
        // 🔥 SINCRONIZAR EQUIPO_ZONA
        // ============================
        List<EquipoZona> participaciones =
                equipoZonaRepository.findByEquipoId(id);

        for (EquipoZona ez : participaciones) {
            ez.setNombreEquipo(dto.getNombre());
        }

        equipoZonaRepository.saveAll(participaciones);

        return EquipoMapper.toDTO(equipo);
    }

    public EquipoDTO asignarCancha(Long idEquipo, Long idCancha) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Cancha cancha = canchaRepository.findById(idCancha)
                .orElseThrow(() -> new RuntimeException("Cancha no encontrada"));

        equipo.setLocalia(cancha);

        Equipo actualizado = equipoRepository.save(equipo);
        return EquipoMapper.toDTO(actualizado);
    }

    public EquipoDTO asignarZona(Long idEquipo, Long idZona) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Zona zona = zonaRepository.findById(idZona)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));



        Equipo actualizado = equipoRepository.save(equipo);
        return EquipoMapper.toDTO(actualizado);
    }

    public EquipoDTO registrarJugador(Long idEquipo, Long idJugador) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));


        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // agregar jugador al equipo
        equipo.addJugador(jugador);
        jugador.setEquipo(equipo);

        jugadorRepository.save(jugador);

        Equipo actualizado = equipoRepository.save(equipo);

        return EquipoMapper.toDTO(actualizado);
    }

    public EquipoDTO eliminarJugador(Long idEquipo, Long idJugador) {

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // Validar que el jugador realmente pertenece a ese equipo
        if (jugador.getEquipo() == null || !jugador.getEquipo().getId().equals(idEquipo)) {
            throw new RuntimeException("El jugador no pertenece a este equipo");
        }

        // Remover desde ambos lados (MUY IMPORTANTE)
        equipo.getJugadores().remove(jugador);
        jugador.setEquipo(null);

        // Guardar cambios
        jugadorRepository.save(jugador);
        Equipo actualizado = equipoRepository.save(equipo);

        return EquipoMapper.toDTO(actualizado);
    }



}
