package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.TorneoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.Torneo;
import com.patricio.springboot.app.entity.Zona;
import com.patricio.springboot.app.mapper.TorneoMapper;
import com.patricio.springboot.app.repository.TorneoRepository;
import com.patricio.springboot.app.repository.ZonaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.patricio.springboot.app.mapper.TorneoMapper.toDTO;
import static java.util.stream.Collectors.toList;

@Service
public class TorneoService {
    private TorneoRepository torneoRepository;
    private ZonaRepository zonaRepository;

    public TorneoService(TorneoRepository torneoRepository, ZonaRepository zonaRepository) {
        this.torneoRepository = torneoRepository;
        this.zonaRepository = zonaRepository;
    }
    // --------------------------
    // CREAR TORNEO
    // --------------------------

    public TorneoDTO crearTorneo(TorneoDTO dto) {

        Torneo torneo = new Torneo();
        torneo.setNombre(dto.getNombre());
        torneo.setDivision(dto.getDivision());
        torneo.setEncargado(dto.getEncargado());
        torneo.setEstado(dto.getEstado());
        torneo.setFechaCreacion(LocalDate.now());

        torneo = torneoRepository.save(torneo);

        return toDTO(torneo);
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

        torneo.setNombre(dto.getNombre());
        torneo.setDivision(dto.getDivision());
        torneo.setEncargado(dto.getEncargado());
        torneo.setEstado(dto.getEstado());

        torneoRepository.save(torneo);

        return toDTO(torneo);
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

    // --------------------------
    // MAPPER
    // --------------------------

}
