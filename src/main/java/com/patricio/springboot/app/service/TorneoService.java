package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.*;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.TorneoMapper;
import com.patricio.springboot.app.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.patricio.springboot.app.mapper.TorneoMapper.toDTO;


@Service
public class TorneoService {
    private final EquipoService equipoService;
    private TorneoRepository torneoRepository;
    private ZonaRepository zonaRepository;
    private UsuarioRepository usuarioRepository;
    private EquipoZonaRepository equipoZonaRepository;
    private EquipoZonaService equipoZonaService;
    private PartidoService partidoService;
    private ProgramacionFechaService programacionService;
    private EtapaTorneoRepository etapaTorneoRepository;

    public TorneoService(TorneoRepository torneoRepository, EtapaTorneoRepository etapaTorneoRepository, ProgramacionFechaService programacionFechaService, ZonaRepository zonaRepository, EquipoZonaRepository equipoZonaRepository, UsuarioRepository usuarioRepository, EquipoZonaService equipoZonaService, PartidoService partidoService, EquipoService equipoService) {
        this.torneoRepository = torneoRepository;
        this.zonaRepository = zonaRepository;
        this.equipoZonaRepository = equipoZonaRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoZonaService = equipoZonaService;
        this.partidoService = partidoService;
        this.equipoService = equipoService;
        this.programacionService = programacionService;
        this.etapaTorneoRepository = etapaTorneoRepository;
    }

    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true)
    })
    public TorneoDTO crearTorneo(TorneoDTO dto, Authentication auth) {

        boolean existeActivo;

// Verificamos si el DTO trae una división real (que no sea null ni texto vacío)
        if (dto.getDivision() != null && !dto.getDivision().trim().isEmpty()) {
            // CASO A: El torneo nuevo TIENE división.
            // Buscamos si ya existe uno con: Mismo Nombre + Misma División + Activo
            existeActivo = torneoRepository.existsByNombreIgnoreCaseAndDivisionAndEstadoIgnoreCase(
                    dto.getNombre().trim(),
                    dto.getDivision().trim(),
                    "activo"
            );
        } else {
            // CASO B: El torneo nuevo NO TIENE división.
            // Buscamos si ya existe uno con: Mismo Nombre + División NULL + Activo
            existeActivo = torneoRepository.existsByNombreIgnoreCaseAndDivisionIsNullAndEstadoIgnoreCase(
                    dto.getNombre().trim(),
                    "activo"
            );
        }

        if (existeActivo) {
            // Personalizamos el mensaje de error para que sea claro
            String errorMsg = "Ya existe un torneo activo con el nombre '" + dto.getNombre() + "'";
            if (dto.getDivision() != null && !dto.getDivision().isEmpty()) {
                errorMsg += " en la división " + dto.getDivision();
            }
            throw new RuntimeException(errorMsg);
        }

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
    public List<TorneoResumenDTO> listarActivos() {
        // Usamos findByEstado (sin cargar zonas) para que sea súper rápido
        return torneoRepository.findByEstadoIgnoreCase("activo")
                .stream()
                .map(TorneoMapper::toResumenDTO) // <--- Usamos el mapper resumen
                .toList();
    }


    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true)
    })
    public TorneoDTO modificarTorneo(Long id, TorneoDTO dto, Authentication auth) {

        // 1. Buscamos el torneo existente
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

// 2. Validamos duplicados (Nombre + División) EXCLUYENDO el ID actual
        boolean existeOtroActivo;

        // Verificamos si el DTO trae una división real
        if (dto.getDivision() != null && !dto.getDivision().trim().isEmpty()) {
            // CASO A: Tiene división. Buscamos duplicado excluyendo este ID
            existeOtroActivo = torneoRepository.existsByNombreIgnoreCaseAndDivisionAndEstadoIgnoreCaseAndIdNot(
                    dto.getNombre().trim(),
                    dto.getDivision().trim(),
                    "activo",
                    id // <--- Importante: Pasamos el ID para excluirlo
            );
        } else {
            // CASO B: No tiene división (es null). Buscamos duplicado excluyendo este ID
            existeOtroActivo = torneoRepository.existsByNombreIgnoreCaseAndDivisionIsNullAndEstadoIgnoreCaseAndIdNot(
                    dto.getNombre().trim(),
                    "activo",
                    id // <--- Importante
            );
        }

        if (existeOtroActivo) {
            String errorMsg = "Ya existe otro torneo activo con el nombre '" + dto.getNombre() + "'";
            if (dto.getDivision() != null && !dto.getDivision().isEmpty()) {
                errorMsg += " en la división " + dto.getDivision();
            }
            throw new RuntimeException(errorMsg);
        }

// 3. Si pasa la validación, procedemos a actualizar
        torneo.setNombre(dto.getNombre());
        torneo.setDivision(dto.getDivision());

        if (dto.getEstado() != null && !dto.getEstado().isBlank()){
            torneo.setEstado(dto.getEstado());
        }
        if (dto.getTipo() != null) {
            torneo.setTipo(dto.getTipo());
        }

        // === AGREGAR ESTO PARA PROCESAR LOS COLORES ===
        torneo.setColorPrimario(dto.getColorPrimario());
        torneo.setColorSecundario(dto.getColorSecundario());
        torneo.setColorTextoPrimario(dto.getColorTextoPrimario());
        torneo.setColorTextoSecundario(dto.getColorTextoSecundario());
// ==============================================
        // === AGREGAR ESTO ===
        torneo.setFotoUrl(dto.getFotoUrl());
        torneo.setGenero(dto.getGenero());
        torneo.setRedSocial(dto.getRedSocial());

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
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "zonasPorTorneo", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true)
    })
    @Transactional // ¡MUY IMPORTANTE! Si algo falla, no borra nada a medias
    public TorneoDTO quitarZona(Long idTorneo, Long idZona) {

        Torneo torneo = torneoRepository.findById(idTorneo)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        Zona zona = zonaRepository.findById(idZona)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        if (!zona.getTorneo().getId().equals(idTorneo)) {
            throw new RuntimeException("La zona no pertenece a este torneo");
        }

        // 1. LIMPIEZA DE DEPENDENCIAS (Evita el error de Foreign Key)
        // Borrar programaciones de los partidos de esta zona

        // Borrar los partidos de esta zona
        partidoService.eliminarProgramacionesDeZona(idZona);

        // Borrar la tabla de posiciones (la foto que me mandaste) de esta zona
        equipoZonaRepository.deleteByZonaId(idZona);

        // 2. BORRADO DE LA ZONA
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
            @CacheEvict(value = "zonasPorTorneo", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true)
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

        // 1. Busqueda inteligente (Slug o ID)
        if (slug.matches("\\d+")) {
            Long id = Long.parseLong(slug);
            torneo = torneoRepository.findByIdOptimized(id)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + id));
        } else {
            torneo = torneoRepository.findBySlugOptimized(slug)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado: " + slug));
        }

        // 2. Lógica de visibilidad (En lugar de seguridad privada)
        // Solo lanzamos error si el torneo está "inactivo" y el usuario no tiene permisos
        if ("inactivo".equalsIgnoreCase(torneo.getEstado())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (auth != null) ? auth.getName() : "";

            boolean esAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean esDuenio = torneo.getEncargado() != null &&
                    torneo.getEncargado().getEmail().equalsIgnoreCase(email);

            if (!esAdmin && !esDuenio) {
                throw new AccessDeniedException("Este torneo aún no es público.");
            }
        }

        // Al usar TorneoMapper.toDTO(torneo), recordá que este DTO SI debe tener las zonas.
        return TorneoMapper.toDTO(torneo);
    }

    @Transactional
    public EtapaTorneo crearEtapa(Long torneoId, String nombre, String tipo, Integer orden) {
        // Validar que los parámetros no vengan nulos desde el DTO
        if (nombre == null || nombre.isEmpty())
            throw new IllegalArgumentException("El nombre de la etapa es obligatorio");
        if (orden == null) throw new IllegalArgumentException("El orden de la etapa es obligatorio");

        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new RuntimeException("No se encontró el torneo con ID: " + torneoId));

        // Validar si ya existe una etapa con ese nombre O con ese orden para evitar conflictos
        Optional<EtapaTorneo> etapaExistente = etapaTorneoRepository.findByTorneoIdAndNombre(torneoId, nombre);
        if (etapaExistente.isPresent()) {
            // En lugar de retornar la existente, podrías lanzar error si quieres avisar al admin
            throw new RuntimeException("La etapa '" + nombre + "' ya existe en este torneo.");
        }

        EtapaTorneo nuevaEtapa = new EtapaTorneo();
        nuevaEtapa.setNombre(nombre);
        nuevaEtapa.setTipo(tipo);
        nuevaEtapa.setOrden(orden);
        nuevaEtapa.setTorneo(torneo);

        try {
            return etapaTorneoRepository.save(nuevaEtapa);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar en base de datos: " + e.getMessage());
        }
    }

    // En TorneoService.java
    @Transactional(readOnly = true)
    public List<EtapaTorneo> obtenerCuadroFaseFinal(Long torneoId) {
        // 1. Buscamos las etapas ordenadas (1: Final, 2: Semis, etc.)
        List<EtapaTorneo> etapas = etapaTorneoRepository.findByTorneoIdOrderByOrdenAsc(torneoId);

        // 2. Forzamos la carga de partidos para cada etapa (o usamos un DTO)
        // Esto evita el problema de que el Front reciba la lista vacía
        etapas.forEach(etapa -> {
            etapa.getPartidos().size(); // Fuerza la carga de la colección
        });

        return etapas;
    }

    @Transactional(readOnly = true)
    public List<EtapaFaseFinalDTO> obtenerEstructuraCuadro(Long torneoId) {
        // 1. Buscamos las etapas ordenadas por su jerarquía (Final, Semis, etc.)
        List<EtapaTorneo> etapas = etapaTorneoRepository.findByTorneoIdOrderByOrdenAsc(torneoId);

        // 2. Mapeamos a DTO recorriendo cada etapa y sus partidos
        return etapas.stream().map(etapa -> {
            EtapaFaseFinalDTO dto = new EtapaFaseFinalDTO();
            dto.setId(etapa.getId());
            dto.setNombre(etapa.getNombre());
            dto.setOrden(etapa.getOrden());

            // Mapeamos los partidos de esta etapa de forma segura
            List<EtapaFaseFinalDTO.PartidoResumenDTO> partidosDTO = etapa.getPartidos().stream().map(p -> {
                EtapaFaseFinalDTO.PartidoResumenDTO pDto = new EtapaFaseFinalDTO.PartidoResumenDTO();
                pDto.setId(p.getId());

                // --- MANEJO SEGURO DE EQUIPO LOCAL ---
                if (p.getEquipoLocal() != null) {
                    pDto.setEquipoLocal(p.getEquipoLocal().getNombre());
                    pDto.setEquipoLocalEscudo(p.getEquipoLocal().getEscudo());
                    pDto.setEquipoLocal(p.getEquipoLocal().getNombre());
                } else {
                    pDto.setEquipoLocal("POR DEFINIR");
                    pDto.setEquipoLocalEscudo(null);
                }

                // --- MANEJO SEGURO DE EQUIPO VISITANTE ---
                if (p.getEquipoVisitante() != null) {
                    pDto.setEquipoVisitante(p.getEquipoVisitante().getNombre());
                    pDto.setEquipoVisitanteEscudo(p.getEquipoVisitante().getEscudo());
                    pDto.setEquipoVisitante(p.getEquipoVisitante().getNombre());
                } else {
                    pDto.setEquipoVisitante("POR DEFINIR");
                    pDto.setEquipoVisitanteEscudo(null);
                }

                // --- GOLES Y RESULTADOS ---
                pDto.setGolesLocal(p.getGolesLocal());
                pDto.setGolesVisitante(p.getGolesVisitante());


                // --- FECHA Y HORA (Null-Safe) ---
                pDto.setFecha(p.getFecha() != null ? p.getFecha().toString() : null);
                pDto.setHora(p.getHora() != null ? p.getHora().toString() : null);

                // --- UBICACIÓN Y ÁRBITRO ---
                if (p.getCancha() != null) {
                    pDto.setCancha(p.getCancha().getNombre());
                } else {
                    pDto.setCancha("SIN ASIGNAR");
                }

                pDto.setArbitro(p.getVeedor()); // O el campo que uses para el árbitro
                pDto.setEstado(p.getEstado());

                // IMPORTANTE: El orden determina la posición en el Cuadro Visual
                pDto.setOrden(p.getOrden());

                return pDto;
            }).collect(Collectors.toList());

            dto.setPartidos(partidosDTO);
            return dto;
        }).collect(Collectors.toList());
    }
}
