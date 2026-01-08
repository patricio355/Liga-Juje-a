package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.EquipoZonaDTO;
import com.patricio.springboot.app.dto.TorneoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.TorneoMapper;
import com.patricio.springboot.app.repository.EquipoZonaRepository;
import com.patricio.springboot.app.repository.TorneoRepository;
import com.patricio.springboot.app.repository.UsuarioRepository;
import com.patricio.springboot.app.repository.ZonaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.patricio.springboot.app.mapper.TorneoMapper.toDTO;
import static java.util.stream.Collectors.toList;

@Service
public class TorneoService {
    private final EquipoService equipoService;
    private TorneoRepository torneoRepository;
    private ZonaRepository zonaRepository;
    private UsuarioRepository usuarioRepository;
    private EquipoZonaRepository equipoZonaRepository;
    private EquipoZonaService equipoZonaService;
    private PartidoService partidoService;

    public TorneoService(TorneoRepository torneoRepository, ZonaRepository zonaRepository, EquipoZonaRepository equipoZonaRepository, UsuarioRepository usuarioRepository, EquipoZonaService equipoZonaService, PartidoService partidoService, EquipoService equipoService) {
        this.torneoRepository = torneoRepository;
        this.zonaRepository = zonaRepository;
        this.equipoZonaRepository = equipoZonaRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoZonaService = equipoZonaService;
        this.partidoService = partidoService;
        this.equipoService = equipoService;
    }

    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true)
    })
    public TorneoDTO crearTorneo(TorneoDTO dto, Authentication auth) {

        Torneo torneo = TorneoMapper.toEntity(dto);
        torneo.setFechaCreacion(LocalDate.now());

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));


        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogueado = usuarioRepository.findByEmail(emailAutenticado).orElseThrow();


        if (usuarioLogueado.getRol().equals("ROLE_ADMIN") && dto.getEncargadoEmail() != null) {

            Usuario encargadoAsignado = usuarioRepository.findByEmail(dto.getEncargadoEmail()).orElseThrow();
            torneo.setEncargado(encargadoAsignado);
        } else {

            torneo.setEncargado(usuarioLogueado);
        }


        torneo.setCreador(usuarioLogueado);


        String emailEncargado;

        if (esAdmin) {

            emailEncargado = dto.getEncargadoEmail();
        } else {

            emailEncargado = auth.getName();
        }

        if (emailEncargado != null && !emailEncargado.isBlank()) {

            if (!emailEncargado.contains("@")) {
                throw new RuntimeException("Email inválido");
            }

            Usuario encargado = usuarioRepository.findByEmail(emailEncargado)
                    .orElseThrow(() ->
                            new RuntimeException("No existe usuario con ese email")
                    );

            if (!encargado.getRol().equals("ENCARGADOTORNEO")) {
                throw new RuntimeException("El usuario no es encargado de torneo");
            }

            torneo.setEncargado(encargado);
        }


        String slug = crearSlugSeguro(torneo.getNombre());
        torneo.setSlug(slug);

        return TorneoMapper.toDTO(torneoRepository.save(torneo));
    }
    public String crearSlugSeguro(String nombreOriginal) {
        // 1. Limpiamos el nombre (minúsculas y guiones)
        String slugBase = nombreOriginal.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String slugFinal = slugBase;
        int contador = 1;

        // 2. Mientras el slug exista en Cloud SQL, le añadimos un número
        while (torneoRepository.existsBySlug(slugFinal)) {
            slugFinal = slugBase + "-" + contador;
            contador++;
        }

        return slugFinal;
    }

    @Cacheable(value = "dashboardTorneos")
    public List<TorneoDTO> listarTorneos() {
        return torneoRepository.findAllWithZonas()
                .stream()
                .map(TorneoMapper::toDTO)
                .toList();
    }


    @Cacheable(value = "torneosActivos")
    public List<TorneoDTO> listarActivos() {
        return torneoRepository.findByEstadoConZonas("activo")
                .stream()
                .map(TorneoMapper::toDTO)
                .toList();
    }


    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", key = "#id") // Limpia el detalle específico
    })
    public TorneoDTO modificarTorneo(Long id, TorneoDTO dto, Authentication auth) {

        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        // =========================
        // DATOS BÁSICOS
        // =========================
        torneo.setNombre(dto.getNombre());
        torneo.setDivision(dto.getDivision());
        torneo.setEstado(dto.getEstado());
        if (dto.getTipo() != null) {
            torneo.setTipo(dto.getTipo());
        }

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // =========================
        // MANEJO ENCARGADO TORNEO
        // =========================
        if (esAdmin) {

            String email = dto.getEncargadoEmail();

            if (email == null || email.isBlank()) {
                torneo.setEncargado(null);
            } else {
                Usuario encargado = usuarioRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException("No existe usuario con ese email")
                        );

                if (!encargado.getRol().equals("ENCARGADOTORNEO")) {
                    throw new RuntimeException("El usuario no es encargado de torneo");
                }

                torneo.setEncargado(encargado);
            }
        }
        // 🔒 si NO es admin, se ignora encargadoEmail

        Torneo actualizado = torneoRepository.save(torneo);
        return TorneoMapper.toDTO(actualizado);
    }



    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", key = "#id") // Limpia el detalle específico
    })
    public void eliminarTorneo(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        torneo.setEstado("inactivo");

        torneoRepository.save(torneo);
    }


    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", key = "#result.slug") // Limpia el detalle del torneo específico
    })
    public TorneoDTO agregarZona(Long idTorneo, ZonaDTO zonaDTO) {

        Torneo torneo = torneoRepository.findById(idTorneo)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        Zona zona = new Zona();
        zona.setNombre(zonaDTO.getNombre());
        zona.setTorneo(torneo);

        zonaRepository.save(zona);

        torneo.getZonas().add(zona);
        torneoRepository.save(torneo);

        return toDTO(torneo);
    }


    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true)
    })
    public TorneoDTO quitarZona(Long idTorneo, Long idZona) {

        Torneo torneo = torneoRepository.findById(idTorneo)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        Zona zona = zonaRepository.findById(idZona)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        if (!zona.getTorneo().getId().equals(idTorneo)) {
            throw new RuntimeException("La zona no pertenece a este torneo");
        }

        torneo.getZonas().remove(zona);
        zonaRepository.delete(zona);

        return TorneoMapper.toDTO(torneo);
    }


    public List<TorneoDTO> torneosDisponiblesParaEquipo(Long equipoId) {

        List<Long> torneosInscripto =
                equipoZonaRepository.findTorneoIdsByEquipoId(equipoId);

        // 🔹 Si no participa en ninguno → todos los torneos activos y abiertos
        if (torneosInscripto.isEmpty()) {
            return torneoRepository
                    .findByEstadoAndTipo("activo", "ABIERTO")
                    .stream()
                    .map(TorneoMapper::toDTO)
                    .toList();
        }

        // 🔹 Torneos activos, abiertos y donde NO participa
        return torneoRepository
                .findByEstadoAndTipoAndIdNotIn(
                        "activo",
                        "ABIERTO",
                        torneosInscripto
                )
                .stream()
                .map(TorneoMapper::toDTO)
                .toList();
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            // CAMBIO: Limpiamos todos los detalles para asegurar que el slug se actualice
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "zonasPorTorneo", allEntries = true)
    })
    public EquipoZonaDTO agregarEquipoAZona(Long equipoId, Long zonaId) {
        // 1. Inscribir equipo (Aquí es donde el Service debe validar si ya existe)
        EquipoZonaDTO dto = equipoZonaService.inscribirEquipo(equipoId, zonaId);

        // 2. Obtener zona + torneo con JOIN FETCH para evitar N+1
        Zona zona = zonaRepository.findByIdOptimized(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        Torneo torneo = zona.getTorneo();

        // 3. Regenerar fixture solo si es ABIERTO
        if ("ABIERTO".equalsIgnoreCase(torneo.getTipo())) {
            partidoService.regenerarFixtureZona(zonaId);
            // Al regenerar partidos, también deberíamos limpiar cachés de fixture si tuvieras
        }

        return dto;
    }



    @Cacheable(value = "dashboardTorneos", key = "#email")
    public List<TorneoDTO> listarTorneosDelEncargado(String email) {
        return torneoRepository.findByEncargadoEmailWithZonas(email)
                .stream()
                .map(TorneoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "torneoDetalle", key = "#id")
    public TorneoDTO obtenerPorId(Long id) {

        Torneo torneo = torneoRepository.findByIdOptimized(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioAutenticado = auth.getName();

        boolean esAdminGlobal = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDuenio = torneo.getEncargado() != null &&
                torneo.getEncargado().getEmail().equalsIgnoreCase(emailUsuarioAutenticado);

        if (!esAdminGlobal && !esDuenio) {
            throw new AccessDeniedException("No tienes permiso para gestionar este torneo.");
        }

        return TorneoMapper.toDTO(torneo);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "torneoDetalle", key = "#slug")
    public TorneoDTO obtenerPorSlug(String slug) {
        Torneo torneo;

        // 1. Verificamos si el 'slug' recibido es en realidad un ID numérico
        if (slug.matches("\\d+")) {
            // Es un número: Buscamos por ID
            Long id = Long.parseLong(slug);
            torneo = torneoRepository.findByIdOptimized(id)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + id));
        } else {
            // Es texto: Buscamos por la columna Slug
            torneo = torneoRepository.findBySlugOptimized(slug)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con el nombre: " + slug));
        }

        // 2. Lógica de seguridad (se mantiene intacta)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioAutenticado = auth.getName();

        boolean esAdminGlobal = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDuenio = torneo.getEncargado() != null &&
                torneo.getEncargado().getEmail().equalsIgnoreCase(emailUsuarioAutenticado);

        if (!esAdminGlobal && !esDuenio) {
            throw new AccessDeniedException("No tienes permiso para ver este torneo.");
        }

        return TorneoMapper.toDTO(torneo);
    }
}
