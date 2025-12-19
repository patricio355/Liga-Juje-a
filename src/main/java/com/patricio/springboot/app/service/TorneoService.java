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
import jakarta.transaction.Transactional;
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
    // --------------------------
    // CREAR TORNEO
    // --------------------------

    public TorneoDTO crearTorneo(TorneoDTO dto) {

        Torneo torneo = TorneoMapper.toEntity(dto);
        torneo.setFechaCreacion(LocalDate.now());

        String email = dto.getEncargadoEmail();

        if (email != null && !email.isBlank()) {

            if (!email.contains("@")) {
                throw new RuntimeException("Email inválido");
            }

            Usuario encargado = usuarioRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("No existe usuario con ese email")
                    );

            if (!encargado.getRol().equals("ENCARGADOTORNEO")) {
                throw new RuntimeException("El usuario no es encargado de torneo");
            }

            torneo.setEncargado(encargado);
        }

        return TorneoMapper.toDTO(torneoRepository.save(torneo));
    }


    // --------------------------
    // LISTAR TORNEOS
    // --------------------------

    public List<TorneoDTO> listarTorneos() {
        return torneoRepository.findAll()
                .stream()
                .map(TorneoMapper::toDTO)
                .toList();
    }


    public List<TorneoDTO> listarActivos() {
        return torneoRepository.findByEstado("activo")
                .stream()
                .map(TorneoMapper::toDTO)
                .toList();
    }

    // --------------------------
    // MODIFICAR TORNEO
    // --------------------------

    public TorneoDTO modificarTorneo(Long id, TorneoDTO dto) {

        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        // =========================
        // DATOS BÁSICOS
        // =========================
        torneo.setNombre(dto.getNombre());
        torneo.setDivision(dto.getDivision());
        torneo.setEstado(dto.getEstado());

        // =========================
        // MANEJO ENCARGADO TORNEO
        // =========================
        String email = dto.getEncargadoEmail();

        if (email == null || email.isBlank()) {
            // quitar encargado
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

        Torneo actualizado = torneoRepository.save(torneo);
        return TorneoMapper.toDTO(actualizado);
    }


    // --------------------------
    // ELIMINAR TORNEO
    // --------------------------

    public void eliminarTorneo(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        torneo.setEstado("inactivo");

        torneoRepository.save(torneo);
    }

    // --------------------------
    // AGREGAR ZONA A TORNEO
    // --------------------------

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

    // --------------------------
    // QUITAR ZONA
    // --------------------------

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
    public EquipoZonaDTO agregarEquipoAZona(Long equipoId, Long zonaId) {

        //  Inscribir equipo en la zona
        EquipoZonaDTO dto = equipoZonaService.inscribirEquipo(equipoId, zonaId);

        //  Obtener zona + torneo
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        Torneo torneo = zona.getTorneo();

        //  Regenerar fixture SOLO si el torneo es ABIERTO
        if ("ABIERTO".equalsIgnoreCase(torneo.getTipo())) {
            partidoService.regenerarFixtureZona(zonaId);
        }

        return dto;
    }
}
